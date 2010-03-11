package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class FtpFetchProtocol implements FetchProtocol
{
    private static final Log log = LogFactory.getLog(FtpFetchProtocol.class);
    
    @Override
    public FileFetcher createFetcher(URI uri, Long size) throws BagTransferException
    {
        return new FtpFetcher();
    }

    private class FtpFetcher extends LongRunningOperationBase implements FileFetcher
    {
        private FTPClient client;
        
        public FtpFetcher()
        {
        }
        
        @Override
        public void initialize() throws BagTransferException
        {
        }
        
        @Override
        public void close()
        {
        	try 
        	{
        		// Set to null first, since it's quite unlikely
        		// to throw an exception.
        		FTPClient currentClient = this.client;
				this.client = null;
				
				currentClient.disconnect();
			}
        	catch (IOException e) 
        	{
        		log.warn("An error occurred while disconnecting.  The error will be ignored.", e);
			}
        }
        
        @Override
        public void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException
        {
            if (this.client == null)
            {
                this.connect(uri);
            }

            log.trace(format("Fetching {0} to destination {1}", uri, destination.getFilepath()));
            
            InputStream in = null;
            OutputStream out = null;

            try
            {
                log.trace("Executing retrieveal.");
                in = this.client.retrieveFileStream(uri.getPath());

                if (in == null)
                {
                	int replyCode = this.client.getReplyCode();
                	String replyString = this.client.getReplyString();
                	
                	throw new BagTransferException(format("Could not retrieve file stream.  Server returned code {0}: {1}", replyCode, replyString));
                }
                
                log.trace("Opening destination.");
                out = destination.openOutputStream(false);

                log.trace("Copying from network to destination.");
                FetchStreamCopier copier = new FetchStreamCopier("Downloading", uri, size);
                this.delegateProgress(copier);
                long bytesCopied = copier.copy(in, out);
                log.trace(format("Successfully copied {0} bytes.", bytesCopied));
                
                this.client.completePendingCommand();
            }
            catch (IOException e)
            {
            	this.close();
                throw new BagTransferException(e);
            }
            finally
            {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        
        private void connect(URI uri) throws BagTransferException
        {
            try
            {
                String server = uri.getHost();
                int port = uri.getPort() < 0 ? FTP.DEFAULT_PORT : uri.getPort();
                
                this.client = new FTPClient();
                this.client.addProtocolCommandListener(new LogCommandListener());
                
                log.trace(format("Connecting to server: {0}:{1}", server, port));
                this.client.connect(server, port);

                this.client.setSoTimeout(20 * 1000);
                this.client.setDataTimeout(20 * 1000);
                log.trace(format("Socket timeout: {0}ms", this.client.getSoTimeout()));
                
                
                int reply = this.client.getReplyCode();
                log.trace(format("Connected.  Server replied with: {0}", reply));
                
                if (!FTPReply.isPositiveCompletion(reply))
                {
                    this.client.disconnect();
                    String msg = MessageFormat.format("Could not connect to FTP server ({0}): {1}", uri); 
                    throw new BagTransferException(msg);
                }
                
                this.login();
                
                log.trace(format("Connected to: {0}", this.client.getSystemName()));
                
                log.trace("Setting PASV mode and setting binary file type.");
                this.client.enterLocalPassiveMode();
                this.client.setFileType(FTP.BINARY_FILE_TYPE);
            }
            catch (IOException e)
            {
                this.client = null;
                throw new BagTransferException(e);
            }
        }
        
        private void login() throws IOException, BagTransferException
        {
        	log.trace("Obtaining credentials.");
            PasswordAuthentication credentials = Authenticator.requestPasswordAuthentication(this.client.getRemoteAddress(), this.client.getRemotePort(), "FTP", "Login", "Password");
            
            try
            {
            	String username;
            	String password;
            	
                if (credentials == null)
                {
                    log.trace("No credentials available.  Login will be anonymous.");
                    username = "anonymous";
                    password = "bagitlibrary@loc.gov";
                }
                else
                {
                	username = credentials.getUserName();
                	
                	// It's unfortunate, but the FTP Library requires a string,
                	// which means we cannot clear the credentials from memory when
                	// we're done.  Oh well.
                	password = new String(credentials.getPassword());
                }
                    
                log.trace(format("Logging in with credentials: {0}/***hidden***", username));
                
                if (!this.client.login(username, password))
                {
                    this.client.disconnect();
                    throw new BagTransferException("Could not log in.");
                }
            }
            finally
            {
            	Arrays.fill(credentials.getPassword(), 'x');
            }
        }
    }
    
    private class LogCommandListener implements ProtocolCommandListener
    {
        @Override
        public void protocolCommandSent(ProtocolCommandEvent event)
        {
        	if (log.isTraceEnabled())
        	{
        		String msg = event.getMessage().trim();
        		
        		if (msg.startsWith("PASS"))
        			msg = "PASS ***hidden***";

        		log.trace(">> " + msg);
        	}
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event)
        {
        	if (log.isTraceEnabled())
        		log.trace("<< " + event.getMessage().trim());
        }
    }
}
