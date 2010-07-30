package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gov.loc.cygwin.Cygpath;
import gov.loc.cygwin.CygwinException;
import gov.loc.repository.bagit.transfer.BagTransferCancelledException;
import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class ExternalRsyncFetchProtocol implements FetchProtocol
{
	public static final String PROP_RSYNC_BINARY = "RsyncBinary";
	
	private static final Log log = LogFactory.getLog(ExternalRsyncFetchProtocol.class);
	private String rsyncPath;
	private boolean sanityChecked = false;
	
	public ExternalRsyncFetchProtocol()
	{
		this.rsyncPath = System.getProperty(PROP_RSYNC_BINARY);
		
		if (this.rsyncPath == null)
		{
			this.rsyncPath = "rsync";
		}
	}
	
	@Override
	public ExternalRsyncFetcher createFetcher(URI uri, Long size) throws BagTransferException
	{
		this.checkRsyncSanity();
		return new ExternalRsyncFetcher();
	}
	
	public synchronized void checkRsyncSanity() throws BagTransferException
	{
		if (!this.sanityChecked)
		{
			this.sanityChecked = true;
			
			log.debug(format("Checking for sanity of rsync: {0}", this.rsyncPath));
			
			CommandLine commandLine = CommandLine.parse(rsyncPath);	
			commandLine.addArgument("--version");
			
			try
			{
				DefaultExecutor executor = new DefaultExecutor();
				executor.setStreamHandler(new PumpStreamHandler(NullOutputStream.NULL_OUTPUT_STREAM));

				log.trace(format("Executing test command line: {0}", commandLine));
				int result = executor.execute(commandLine);
				
				if (result == 0)
				{
					log.debug("Rsync test successful.");
				}
				else
				{
					log.warn(format("{0}: returned non-zero exit code during sanity check: {1}", this.rsyncPath, result));
				}
			}
			catch (ExecuteException e)
			{
				throw new BagTransferException(format("Unable to execute rsync: {0}", this.rsyncPath), e);
			}
			catch (IOException e)
			{
				throw new BagTransferException(format("Unable to execute rsync: {0}", this.rsyncPath), e);
			}
		}
	}
	
	private class ExternalRsyncFetcher extends LongRunningOperationBase implements FileFetcher
	{
		private SimpleProcessDestroyer processDestroyer;
		
		@Override
		public void initialize() throws BagTransferException
		{
		}
		
		@Override
		public void close()
		{
		}
		
		@Override
		public void cancel()
		{
			super.cancel();
			
			SimpleProcessDestroyer destroyer = this.processDestroyer;
			
			if (destroyer != null)
			{
				destroyer.destroyProcesses();
			}
		}

		@Override
		public void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException
		{
			File downloadFile;
			
			try
			{
				// Try to do our best to download the data in the actual location
				// it will be used.  Rsync will automatically do temp-file download
				// handling, so we just need to point it at the final file location,
				// and make sure 
				if (destination.getSupportsDirectAccess())
				{
					log.trace("Creating direct-access file in destination location.");
					downloadFile = new File(destination.getDirectAccessPath());
					
					File containingDirectory = downloadFile.getParentFile();
					
					if (!containingDirectory.exists())
					{
						log.trace(format("Creating directory: {0}", containingDirectory.getAbsolutePath()));
						
						if (!containingDirectory.mkdirs())
							log.debug(format("Unable to create parent directory when downloading file (Maybe somebody created it before us?): {0}", destination.getDirectAccessPath()));
					}
				}
				else
				{
					log.warn("File destination does not support direct-access files.  Temporary data will be downloaded to the system temp location, and then copied into its final location.");
					String baseName = FilenameUtils.getBaseName(destination.getFilepath());
					downloadFile = File.createTempFile(baseName, "-bagit.tmp");
				}

				log.trace(format("Created rsync destination file: {0}", downloadFile.getAbsolutePath()));
			}
			catch (IOException e)
			{
				throw new BagTransferException("Unable to create temp file.", e);
			}
			
			// Unescape the URI string.  See Ticket #728
			String srcUriString = this.decodeUri(uri);
						
			CommandLine commandLine = CommandLine.parse(rsyncPath);
			commandLine.addArgument("--quiet");
			commandLine.addArgument("--times");
			commandLine.addArgument(srcUriString, false);
			commandLine.addArgument(this.getLocalPath(downloadFile), false);
			
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			PumpStreamHandler streamHandler = new PumpStreamHandler(NullOutputStream.NULL_OUTPUT_STREAM, err);
			OutputStream destinationStream = null;
			InputStream tempStream = null;
			
			try
			{
				DefaultExecutor executor = new DefaultExecutor();
				executor.setStreamHandler(streamHandler);

				this.processDestroyer = new SimpleProcessDestroyer();
				executor.setProcessDestroyer(this.processDestroyer);

				log.debug(format("Executing: {0}", commandLine.toString()));
				executor.execute(commandLine);
				log.trace("External process exited successfully.");
				
				// Only copy the bytes over if the filesystem doesn't do direct access.
				// If it does support direct access, then rsync takes care of the
				// temp files and re-names for us.
				if (!destination.getSupportsDirectAccess())
				{
					log.debug("Destination does not support direct access.  Copying from temporary location to destination.");
					
					log.trace("Opening destination.");
					destinationStream = destination.openOutputStream(false);
					log.trace("Opening destination file.");
					tempStream = new BufferedInputStream(new FileInputStream(downloadFile));

					log.trace("Copying temp file to destination.");
					FetchStreamCopier copier = new FetchStreamCopier("Copying from temp", destination.getFilepath(), downloadFile.length());
					this.delegateProgress(copier);
					long bytesCopied = copier.copy(tempStream, destinationStream);
					log.debug(format("Successfully copied {0} bytes.", bytesCopied));
				}
			}
			catch (ExecuteException e)
			{
				if (this.isCancelled())
					throw new BagTransferCancelledException();
				
				String error = new String(err.toByteArray());
				String msg = format("An error occurred while executing command line \"{0}\": {1}", commandLine.toString(), error);
				throw new BagTransferException(msg, e);
			}
			catch (IOException e)
			{
				if (this.isCancelled())
					throw new BagTransferCancelledException();
				
				throw new BagTransferException(format("Unexpected exception when executing command: {0}", commandLine.toString()));
			}
			finally
			{
				IOUtils.closeQuietly(tempStream);
				IOUtils.closeQuietly(destinationStream);
			
				if (!destination.getSupportsDirectAccess())
				{
					log.trace("Deleting temporary file.");
					if (!downloadFile.delete())
					{
						log.trace("Unable to delete temporary file.  Marking for delete on exit.");
						downloadFile.deleteOnExit();
					}
				}
			}
		}
		
		private String decodeUri(URI uri) throws BagTransferException
		{
			String uriString = uri.toString();
			
			try
			{
				// UTF-8 is specfied, as per
				// http://java.sun.com/javase/6/docs/api/java/net/URLDecoder.html#decode(java.lang.String,%20java.lang.String)
				uriString = URLDecoder.decode(uriString, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				throw new BagTransferException(e);
			}
			
			return uriString;
		}
		
		private String getLocalPath(File file)
		{
			String finalPath;
			
			if (OS.isFamilyWindows())
			{
				// We've got to be running under Cygwin.
				// We'll better handle non-Cygwin cases later, perhaps.
				
				try
				{
					finalPath = Cygpath.toUnix(file.getAbsolutePath());
				}
				catch (CygwinException e)
				{
					log.warn(format("Unable to convert path using cygpath.  Falling back to simple slash conversion."), e);
					finalPath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
					log.trace(format("Fallback final path: {0}", finalPath));
				}
			}
			else
			{
				finalPath = file.getAbsolutePath();
				log.trace(format("Not on Windows.  Final path is: {0}", finalPath));
			}
			
			return finalPath;
		}
		
		private class SimpleProcessDestroyer implements ProcessDestroyer
		{
			private ArrayList<Process> processesToKill = new ArrayList<Process>(1);
			
			public void destroyProcesses()
			{
				for (Process process : this.processesToKill)
				{
					process.destroy();
				}
			}
			
			@Override
			public boolean add(Process process)
			{
				return this.processesToKill.add(process);
			}

			@Override
			public boolean remove(Process process)
			{
				return this.processesToKill.remove(process);
			}

			@Override
			public int size()
			{
				return this.processesToKill.size();
			}
			
		}
	}
}
