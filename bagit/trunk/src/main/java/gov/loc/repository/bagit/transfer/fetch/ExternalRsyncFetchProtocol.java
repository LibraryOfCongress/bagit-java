package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gov.loc.cygwin.Cygpath;
import gov.loc.cygwin.CygwinException;
import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;

public class ExternalRsyncFetchProtocol implements FetchProtocol
{
	public static final String PROP_RSYNC_BINARY = "RsyncBinary";
	
	private static final Log log = LogFactory.getLog(ExternalRsyncFetchProtocol.class);
	private String rsyncPath;
	
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
		return new ExternalRsyncFetcher();
	}
	
	private class ExternalRsyncFetcher implements FileFetcher
	{
		@Override
		public void initialize() throws BagTransferException
		{
		}
		
		@Override
		public void close()
		{
		}

		@Override
		public void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException
		{
			File tempFile;
			
			try
			{
				log.trace("Creating temp file.");
				tempFile = File.createTempFile("bagit-temp-", ".tmp");
				log.trace(format("Created temp file: {0}", tempFile.getAbsolutePath()));
			}
			catch (IOException e)
			{
				throw new BagTransferException("Unable to create temp file.", e);
			}
			
			CommandLine commandLine = CommandLine.parse(rsyncPath);
			commandLine.addArgument("--quiet");
			commandLine.addArgument("--times");
			commandLine.addArgument(uri.toString());
			commandLine.addArgument(this.getLocalPath(tempFile));
			
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			PumpStreamHandler streamHandler = new PumpStreamHandler(NullOutputStream.NULL_OUTPUT_STREAM, err);
			OutputStream destinationStream = null;
			InputStream tempStream = null;
			
			try
			{
				DefaultExecutor executor = new DefaultExecutor();
				executor.setStreamHandler(streamHandler);

				log.debug(format("Executing: {0}", commandLine.toString()));
				executor.execute(commandLine);
				
				log.trace("Opening destination.");
				destinationStream = destination.openOutputStream(false);
				log.trace("Opening temp file.");
				tempStream = new BufferedInputStream(new FileInputStream(tempFile));
				
				log.trace("Copying temp file to destination.");
				long bytesCopied = IOUtils.copyLarge(tempStream, destinationStream);
				log.debug(format("Successfully copied {0} bytes.", bytesCopied));
			}
			catch (ExecuteException e)
			{
				String error = new String(err.toByteArray());
				String msg = format("An error occurred while executing command line \"{0}\": {1}", commandLine.toString(), error);
				throw new BagTransferException(msg, e);
			}
			catch (IOException e)
			{
				throw new BagTransferException(format("Unexpected exception when executing command: {0}", commandLine.toString()));
			}
			finally
			{
				IOUtils.closeQuietly(tempStream);
				IOUtils.closeQuietly(destinationStream);
			
				log.trace("Deleting temporary file.");
				if (!tempFile.delete())
				{
					log.trace("Unable to delete temporary file.  Marking for delete on exit.");
					tempFile.deleteOnExit();
				}
			}
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
	}
}
