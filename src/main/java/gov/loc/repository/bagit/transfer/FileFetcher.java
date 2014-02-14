package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;
import java.net.URI;

/**
 * Fetches files from a remote destination.
 * 
 * <h3>Threading</h3>
 * <p>A multi-threading retriever may run several FileFetchers
 * of the same type against the same server simultaneously.  However,
 * a given FileFetcher may be asked to retrieve multiple files
 * sequentially, and so may leave persistent connections to
 * remote servers open for performance.  However, it is guaranteed
 * that a given FileFetcher instance will not have its
 * {@link #fetchFile(URI, Long, FetchedFileDestination, FetchContext) fetchFile()} method
 * called in any thread before the current invocation completes.</p>
 * 
 * <h3>Retry</h3>
 * <p>It is up to the implementation of this interface to handle
 * fetch retries as appropriate for their protocol.  A failed fetch
 * attempt should only throw a {@link BagTransferException} after
 * all retry and error correction has been exhausted.</p>
 * <p>If a particular invocation of
 * {@link #fetchFile(URI, Long, FetchedFileDestination, FetchContext) fetchFilefetchFile()}
 * fails and throws a {@link BagTransferException}, the <code>FileFetcher</code>
 * should be prepared to accept additional calls to
 * {@link #fetchFile(URI, Long, FetchedFileDestination, FetchContext) fetchFile()}.
 * Only after the {@link #close()} method is called will there be no more
 * files to fetch.</p>
 * 
 * @see #fetchFile(URI, Long, FetchedFileDestination, FetchContext)
 * @see BagTransferException
 */
public interface FileFetcher extends Cancellable, ProgressListenable
{
	/**
	 * Permits the FileFetcher to initialize.  Guaranteed to be called
	 * prior to any calls to
	 * {@link #fetchFile(URI, Long, FetchedFileDestination, FetchContext) fetchFile()}.
	 * @throws BagTransferException Thrown if an error occurs during initialization.
	 */
	void initialize() throws BagTransferException;
	
	/**
	 * Permits the FileFetcher to clean up.  Guaranteed to not be called
	 * until all calls to
	 * {@link #fetchFile(URI, Long, FetchedFileDestination, FetchContext) fetchFile()}
	 * are completed, and no more class to
	 * {@link #fetchFile(URI, Long, FetchedFileDestination, FetchContext) fetchFile()}
	 * will occur after the call to {@link #close()}.
	 */
    void close();

    /**
     * Set the username if credentials are required to fetch the remote file.
     * E.g. if the file to be fetched is secured on a HTTP server, credentials 
     * are required to connect to the HTTP server.  They are also required is the file
     * is on a FTP server.
     * 
     * If credentials are not applicable, do nothing in the implementation of this method.
     * 
     * @param username The username
     */
    void setUsername(String username);
    
    /**
     * Set the password if credentials are required to fetch the remote file.
     * E.g. if the file to be fetched is secured on a HTTP server, credentials 
     * are required to connect to the HTTP server.  They are also required is the file
     * is on a FTP server.
     * 
     * If credentials are not applicable, do nothing in the implementation of this method.
     *
     * @param password The password
     */
    void setPassword(String password);
    
    /**
     * Fetches a single file.  Each call to this method corresponds to
     * the desire to fetch a single line from a fetch.txt.
	 *
     * @param uri The URI to be fetched.  If the URI scheme cannot be handled by this
     * 			  fetcher, then a {@link BagTransferException} should be thrown.
     * @param size The size of the file.  If no size was specified, this will be <code>null</code>.
     * @param destination The destination for the fetched file.
     * @param context The context for this fetch.
     * @throws BagTransferException Thrown if the file could not be fetched for any reason.
     */
	void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException;
}
