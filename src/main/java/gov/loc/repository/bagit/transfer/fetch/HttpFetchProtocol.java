package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.format;
import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

public class HttpFetchProtocol implements FetchProtocol
{
    private static final Log log = LogFactory.getLog(HttpFetchProtocol.class);
    
	private ThreadSafeClientConnManager connectionManager;
    private final DefaultHttpClient client;
    
    public HttpFetchProtocol()
    {
        this.connectionManager = new ThreadSafeClientConnManager();
        this.client = new DefaultHttpClient(this.connectionManager);
        this.client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "BagIt Library Parallel Fetcher");

        // Since we control the threading manually, set the max
        // configuration values to Very Large Numbers.
        this.connectionManager.setDefaultMaxPerRoute(Integer.MAX_VALUE);
        this.connectionManager.setMaxTotal(Integer.MAX_VALUE);

    	// Set the socket timeout, so that it does not default to infinity.
    	// Otherwise, broken TCP steams can hang threads forever.
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 20 * 1000);
		client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20 * 1000);
    }
    
	public void setRelaxedSsl(boolean relaxedSsl)
	{
		SSLSocketFactory sf;
		if (relaxedSsl) {
			try {
				sf = new SSLSocketFactory(
				        new TrustSelfSignedStrategy(),
				        SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (KeyManagementException e) {
				throw new RuntimeException(e);
			} catch (UnrecoverableKeyException e) {
				throw new RuntimeException(e);
			} catch (KeyStoreException e) {
				throw new RuntimeException(e);
			}
		} else {
			sf = SSLSocketFactory.getSocketFactory();
		}
		Scheme https = new Scheme("https", 443, sf);

		this.connectionManager.getSchemeRegistry().register(https);
	}
    
    @Override
    public FileFetcher createFetcher(URI uri, Long size) throws BagTransferException
    {
        return new HttpFetcher();
    }
    
    private class HttpFetcher extends LongRunningOperationBase implements FileFetcher
    {
    	private String username = null;
    	private String password = null;
    	
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
            
            HttpGet method = new HttpGet(uri);
            
            InputStream in = null;
            OutputStream out = null;
            
            try
            {
                //Set credentials on HttpClient if presented
            	if(this.username != null && this.password != null){
                	client.getCredentialsProvider().setCredentials(
                			AuthScope.ANY,
                			new UsernamePasswordCredentials(this.username, this.password));
                	HttpClientParams.setAuthenticating(client.getParams(), true);
                	log.trace("Setting credentials for HttpClient.");
                } else {
                	HttpClientParams.setAuthenticating(client.getParams(), false);
                }
                
            	log.trace("Executing GET.");
                HttpResponse resp = client.execute(method);
                log.trace(format("Server said: {0}", resp.getStatusLine().toString()));
                
                if (resp.getStatusLine() == null || resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                    throw new BagTransferException(format("Server returned code {0}: {1}", resp.getStatusLine() != null ? resp.getStatusLine().getStatusCode() : "nothing", uri));

                log.trace("Opening destination.");
                out = destination.openOutputStream(false);
                in = resp.getEntity().getContent();
                
                log.trace("Copying from network to destination.");
                FetchStreamCopier copier = new FetchStreamCopier("Downloading", uri, size);
                this.delegateProgress(copier);
                long bytesCopied = copier.copy(in, out);
                log.trace(format("Successfully copied {0} bytes.", bytesCopied));
            }
            catch (IOException e)
            {
            	log.warn("Caught IOException.", e);
                throw new BagTransferException(format("Could not transfer URI: {0}", uri), e);
            }
            catch (RuntimeException e)
            {
            	log.warn("Caught RuntimeException.", e);
            	method.abort();
                throw new BagTransferException(format("Could not transfer URI: {0}", uri), e);
            }            
            finally
            {                
                log.trace("Closing input stream.");
                IOUtils.closeQuietly(in);

                log.trace("Closing output stream.");
                IOUtils.closeQuietly(out);
                
                log.trace("Exiting finally clause.");
            }
        }

		@Override
		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public void setUsername(String username) {
			this.username = username;
		}
    }
}
