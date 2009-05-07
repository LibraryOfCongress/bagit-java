package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.Log;
import gov.loc.repository.bagit.utilities.LogFactory;

public class FtpFetchProtocol implements FetchProtocol
{
    private static final Log log = LogFactory.getLog(FtpFetchProtocol.class);
    
    private String username;
    private String password;
    
    @Override
    public FileFetcher createFetcher(URI uri, Long size) throws BagTransferException
    {
        return new FtpFetcher();
    }

    private class FtpFetcher implements FileFetcher
    {
        private FTPClient client;
        
        public FtpFetcher()
        {
        }
        
        @Override
        public void fetchFile(URI uri, Long size, FetchedFileDestination destination) throws BagTransferException
        {
            if (this.client == null)
            {
                this.connect(uri);
            }

            log.trace("Fetching {0} to destination {1}", uri, destination.getFilepath());
            
            InputStream in = null;
            OutputStream out = null;

            try
            {
                log.trace("Executing retrieveal.");
                in = this.client.retrieveFileStream(uri.getPath());
                
                log.trace("Opening destination.");
                out = destination.openOutputStream(false);

                log.trace("Copying from network to destination.");
                long bytesCopied = IOUtils.copyLarge(in, out);
                log.trace(format("Successfully copied {0} bytes.", bytesCopied));
            }
            catch (IOException e)
            {
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
                
                log.trace("Connecting to server: {0}:{1}", server, port);
                this.client.connect(server, port);
                
                int reply = this.client.getReplyCode();
                log.trace("Connected.  Server replied with: {0}", reply);
                
                if (!FTPReply.isPositiveCompletion(reply))
                {
                    this.client.disconnect();
                    String msg = MessageFormat.format("Could not connect to FTP server ({0}): {1}", uri); 
                    throw new BagTransferException(msg);
                }
                
                log.trace("Logging in with credentials: {0}/xxxxx", username);
                if (!this.client.login(username, password))
                {
                    this.client.disconnect();
                    throw new BagTransferException("Could not log in.");
                }
                
                log.trace("Connected to: {0}", this.client.getSystemName());
                
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
    }
    
    private class LogCommandListener implements ProtocolCommandListener
    {
        @Override
        public void protocolCommandSent(ProtocolCommandEvent event)
        {
            log.trace(">> {0}", event);
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event)
        {
            log.trace("<< {0}", event);
        }
    }
}
