package gov.loc.repository.bagit.transfer.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import static java.text.MessageFormat.format;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

@SuppressWarnings("serial")
public class HttpFetchProtocol implements FetchProtocol
{
    private static final Log log = LogFactory.getLog(HttpFetchProtocol.class);
    
    public HttpFetchProtocol()
    {
        this.connectionManager = new MultiThreadedHttpConnectionManager();
        this.client = new HttpClient(this.connectionManager);
        this.state = new HttpState();

        // Since we control the threading manually, set the max
        // configuration values to Very Large Numbers.
    	this.connectionManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, Integer.MAX_VALUE);
    	this.connectionManager.getParams().setMaxTotalConnections(Integer.MAX_VALUE);

        // If there are credentials present, then set up for premptive authentication.
        PasswordAuthentication auth = Authenticator.requestPasswordAuthentication("remote", null, 80, "http", "", "scheme");
        
        if (auth != null)
        {
        	log.debug(format("Setting premptive authentication using username and password: {0}/xxxxx", auth.getUserName()));
        	state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth.getUserName(), new String(auth.getPassword())));
        	this.defaultParams.setAuthenticationPreemptive(true);
        	this.doAuthentication = true;
        }
        else
        {
        	this.defaultParams.setAuthenticationPreemptive(false);
        	this.doAuthentication = false;
        }
        
        // There's no state in this class right now, so just
        // return the same one over and over.
        this.instance = new HttpFetcher();
    }
    
    @Override
    public FileFetcher createFetcher(URI uri, Long size) throws BagTransferException
    {
        return this.instance;
    }

    private final HttpClientParams defaultParams = new HttpClientParams() {{
        setParameter(USER_AGENT, "BagIt Library Parallel Fetcher ($Id$)");
    }};
    
    private final MultiThreadedHttpConnectionManager connectionManager;
    private final HttpClient client;
    private final HttpState state;
    private final HttpFetcher instance;
    private final boolean doAuthentication;
    
    private class HttpFetcher extends LongRunningOperationBase implements FileFetcher
    {
    	public void initialize() throws BagTransferException
    	{
    	}
    	
    	public void close()
    	{
    	}
    	
        @Override
        public void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException
        {
            log.trace(format("Fetching {0} to destination {1}", uri, destination.getFilepath()));
            
            GetMethod method = new GetMethod(uri.toString());
            method.setParams(defaultParams);
            method.setDoAuthentication(doAuthentication);
            
            InputStream in = null;
            OutputStream out = null;
            
            try
            {
                log.trace("Executing GET");
                int responseCode = client.executeMethod(HostConfiguration.ANY_HOST_CONFIGURATION, method, state);
                log.trace(format("Server said: {0}", method.getStatusLine()));
                
                if (responseCode != HttpStatus.SC_OK)
                    throw new BagTransferException(format("Server returned code {0}: {1}", responseCode, uri));

                log.trace("Opening destination.");
                out = destination.openOutputStream(false);
                in = method.getResponseBodyAsStream();
                
                log.trace("Copying from network to destination.");
                FetchStreamCopier copier = new FetchStreamCopier("Downloading", uri, size);
                this.delegateProgress(copier);
                long bytesCopied = copier.copy(in, out);
                log.trace(format("Successfully copied {0} bytes.", bytesCopied));
            }
            catch (HttpException e)
            {
                throw new BagTransferException(format("Could not transfer URI: {0}", uri), e);
            }
            catch (IOException e)
            {
                throw new BagTransferException(format("Could not transfer URI: {0}", uri), e);
            }
            finally
            {
                method.releaseConnection();
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }
}
