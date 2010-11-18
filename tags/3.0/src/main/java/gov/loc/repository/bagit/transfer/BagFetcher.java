package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.impl.ValidHoleyBagVerifier;

import static java.text.MessageFormat.format;

import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fetches a bag.  This class is not thread-safe.
 * 
 * <p>The default {@link FetchFailStrategy failure strategy} for
 * the fetcher will be {@link StandardFailStrategies#FAIL_FAST}.
 * A new failure strategy may set with the
 * {@link #setFetchFailStrategy(FetchFailStrategy) setFetchFailStrategy()}
 * method.</p>
 * 
 * @author Brian Vargas
 * @version $Id$
 * @see FetchFailStrategy
 * @see StandardFailStrategies
 */
public final class BagFetcher implements Cancellable, ProgressListenable
{
    private static final Log log = LogFactory.getLog(BagFetcher.class);

    // Configurable from outside.
    private int numberOfThreads;
    private FetchFailStrategy failStrategy = StandardFailStrategies.FAIL_FAST;  // As per docs above.
    private FetchedFileDestinationFactory destinationFactory;
    private Map<String, FetchProtocol> protocolFactories = Collections.synchronizedMap(new HashMap<String, FetchProtocol>());
    private BagFactory bagFactory;
    private boolean isCancelled = false;
    private List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();

    // Internal state.
    private Bag bagToFetch;
    private List<FetchTxt.FilenameSizeUrl> fetchTargets;
    private List<FetchTxt.FilenameSizeUrl> failedFetchTargets;
    private AtomicInteger nextFetchTargetIndex;
    private List<BagFile> newBagFiles;
    private List<Fetcher> runningFetchers = new ArrayList<Fetcher>();
    
    public BagFetcher(BagFactory bagFactory) {
    	this.bagFactory = bagFactory;
    	this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    
    @Override
    public void cancel()
    {
    	log.info("Cancelled.");
    	this.isCancelled = true;
    	
    	for (Fetcher fetcher : this.runningFetchers)
    	{
    		fetcher.cancel();
    	}
    }
    
    @Override
    public boolean isCancelled()
    {
    	return this.isCancelled;
    }
    
    @Override
    public void addProgressListener(ProgressListener progressListener)
    {
    	this.progressListeners.add(progressListener);
    }
    
    @Override
    public void removeProgressListener(ProgressListener progressListener)
    {
    	this.progressListeners.remove(progressListener);
    }
    
    private void progress(String activity, Object item, Long count, Long total)
    {
    	for (ProgressListener listener : this.progressListeners)
    	{
    		listener.reportProgress(activity, item, count, total);
    	}
    }
    
    public int getNumberOfThreads()
    {
        return this.numberOfThreads;
    }
    
    public void setNumberOfThreads(int numberOfThreads)
    {
        if (this.numberOfThreads < 1)
            throw new IllegalArgumentException(format("Number of threads cannot be less than 1: {0}", numberOfThreads));
        
        this.numberOfThreads = numberOfThreads;
    }
    
    /**
     * Gets the current fetch failure strategy.
     * @return The currently set failure strategy.
     * 		   Will never be <c>null</c>.
     */
    public FetchFailStrategy getFetchFailStrategy()
    {
    	return this.failStrategy;
    }
    
    /**
     * Sets the failure strategy for this fetcher instance.
     * The failure strategy should be set prior to beginning
     * the actual fetch operation.  Behavior is undefined
     * if the strategy is set after the
     * {@link #fetch(Bag, FetchedFileDestinationFactory) fetch()}
     * method has been called. 
     * 
     * @param strategy The new strategy to use.  Cannot be <c>null</c>.
     * @throws NullPointerException Thrown if <c>null</c> is set.  
     */
    public void setFetchFailStrategy(FetchFailStrategy strategy)
    {
    	if (strategy == null)
    		throw new NullPointerException("strategy cannot be null");
    	
    	this.failStrategy = strategy;
    }
        
    public void registerProtocol(String scheme, FetchProtocol protocol)
    {
        String normalizedScheme = scheme.toLowerCase();
        this.protocolFactories.put(normalizedScheme, protocol);
    }
    
    public BagFetchResult fetch(Bag bag, FetchedFileDestinationFactory destinationFactory) throws BagTransferException
    {
        this.bagToFetch = bag;
        this.destinationFactory = destinationFactory;
        
        this.checkBagSanity();
        
        this.buildFetchTargets();
        this.nextFetchTargetIndex = new AtomicInteger(0);
        
        // Parts of the new bag.
        this.newBagFiles = Collections.synchronizedList(new ArrayList<BagFile>(this.fetchTargets.size()));
        this.failedFetchTargets = Collections.synchronizedList(new ArrayList<FetchTxt.FilenameSizeUrl>());

        BagFetchResult finalResult = new BagFetchResult(true);
        
        if (this.numberOfThreads > 1)
        {
            ExecutorService threadPool = Executors.newCachedThreadPool();
            
            try
            {        
            	log.debug(format("Submitting {0} jobs.", this.numberOfThreads));
            	
                ArrayList<Future<SimpleResult>> futureResults = new ArrayList<Future<SimpleResult>>();                

            	for (int i = 0; i < this.numberOfThreads; i++)
                {
                	log.trace(format("Submitting job {0} of {1}.", i + 1, this.numberOfThreads));
                	Fetcher newFetcher = new Fetcher();
                	this.runningFetchers.add(newFetcher);
                    futureResults.add(threadPool.submit(newFetcher));
                }
                
            	log.debug("Jobs submitted.  Waiting on results.");
            	
                for (Future<SimpleResult> futureResult : futureResults)
                {
                    try
                    {
                        SimpleResult result = futureResult.get();
                        finalResult.merge(result);
                    }
                    catch (ExecutionException e)
                    {
                        String msg = format("An unexpected exception occurred while processing the transfers: {0}", e.getCause().getMessage());
                        finalResult.addMessage(msg);
                        finalResult.setSuccess(false);
                        log.error(msg, e);
                    }
                    catch (InterruptedException e)
                    {
                        String msg = format("Interrupted while waiting for the child threads to complete: {0}", e.getMessage());
                        finalResult.addMessage(msg);
                        finalResult.setSuccess(false);
                        log.error(msg, e);
                    }
                }
            }
            finally
            {
            	log.trace("Shutting down thread pool.");
                threadPool.shutdown();
            	log.trace("Shutting down thread pool.");
            }
        }
        else
        {
        	log.debug("Fetching in single-threaded mode.");
        	
        	try
        	{
	            Fetcher fetcher = new Fetcher();
	            SimpleResult result = fetcher.call();
	            finalResult.merge(result);
        	}
        	catch (Exception e)
        	{
        		throw new BagTransferException("Caught unexpected exception from fetcher.", e);
        	}
        }
        
        // Clone the existing bag, and set it to be returned.
        log.trace("Creating new bag to return.");
        Bag resultBag = this.bagFactory.createBag(bag);
        finalResult.setResultingBag(resultBag);

        // Add in the new bag files.
        resultBag.putBagFiles(this.newBagFiles);
        
        // And reset the fetch.txt, and add back any that failed.
        log.trace(format("Adding {0} files that failed to fetch to the new bag.", this.failedFetchTargets.size()));
        
        if (this.failedFetchTargets.size() > 0)
        {
            if (resultBag.getFetchTxt() == null)
            {
                resultBag.putBagFile(resultBag.getBagPartFactory().createFetchTxt());
            }

            resultBag.getFetchTxt().clear();
            resultBag.getFetchTxt().addAll(this.failedFetchTargets);
        }
        
        log.debug(format("Fetch completed with result: {0}", finalResult.isSuccess()));
        return finalResult;
    }
    
    private void checkBagSanity() throws BagTransferException
    {
    	log.debug("Checking sanity of bag prior to fetch.");
    	
    	SimpleResult verifyResult = this.bagToFetch.verify(new ValidHoleyBagVerifier());
    	
    	if (!verifyResult.isSuccess())
    	{
    		throw new BagTransferException(format("Bag is not valid: {0}", verifyResult.messagesToString()));
    	}
    }
    
    private void buildFetchTargets()
    {
        // Retrieve the fetch items into a seperate list, and then sort the
        // list by descending size.  We want to transfer the big, hard
        // stuff first.  Also, this will help make sure that threads get
        // evenly loaded.
        this.fetchTargets = new ArrayList<FetchTxt.FilenameSizeUrl>(this.bagToFetch.getFetchTxt());
        
        log.trace(format("Sorting fetch target list with {0} items.", fetchTargets.size()));
        Collections.sort(fetchTargets, new FetchSizeSorter());
        log.trace("Sort complete.");
    }
    
    private FetchTxt.FilenameSizeUrl getNextFetchItem()
    {
        FetchTxt.FilenameSizeUrl nextItem;
        
        int next = this.nextFetchTargetIndex.getAndIncrement();
        int size = this.fetchTargets.size();
        
        
        if (next < size)
        {
            nextItem = this.fetchTargets.get(next);
            log.trace(format("Fetching {0}/{1}: {2}", next + 1, size, nextItem.getUrl()));
            this.progress("starting fetch", nextItem.getUrl(), (long)next + 1, (long)size);
        }
        else
        {
            nextItem = null;
            log.trace("Nothing left to fetch.  Returning null.");
        }
        
        return nextItem;
    }
    
    private FileFetcher newFileFetcher(URI uri, Long size) throws BagTransferException
    {
    	String scheme = uri.getScheme();

    	log.trace(format("Getting fetcher for scheme: {0}", scheme));
    	FetchProtocol factory = this.protocolFactories.get(scheme);
        
        if (factory == null)
            throw new BagTransferException(format("No registered factory for URI: {0}", uri));
        
        return factory.createFetcher(uri, size);
    }
    
    private URI parseUri(String uriString) throws BagTransferException
    {
        try
        {
            return new URI(uriString);
        }
        catch (URISyntaxException e)
        {
            String msg = format("Invalid target URL: {0}", uriString);
            log.error(msg, e);
            throw new BagTransferException(msg, e);
        }
    }
    
    private class Fetcher implements Callable<SimpleResult>
    {
    	private SimpleResult result = new SimpleResult(true);
    	private Map<String, FileFetcher> fetchers = new HashMap<String, FileFetcher>();
    	
    	public synchronized void cancel()
    	{
    		for (FileFetcher fetcher : this.fetchers.values())
    		{
    			if (!fetcher.isCancelled())
    				fetcher.cancel();
    		}
    	}
    	
        public SimpleResult call()
        {
        	log.trace("Internal fetcher started.");
        	
        	try
        	{
	            FetchTxt.FilenameSizeUrl target = getNextFetchItem();
	            
	            while (target != null && !isCancelled())
	            {
	            	FetchedFileDestination destination = null;
	            	
	                try
	                {
	                	// The fetch.txt line parts.
		                URI uri = parseUri(target.getUrl());
		                Long size = target.getSize();
		                String destinationPath = target.getFilename();
		                
		                // Create the destination for the file.
		                log.trace(format("Creating destination: {0}", destinationPath));
		                destination = destinationFactory.createDestination(destinationPath, size);

		                // Create the object to do the fetching.
		                FileFetcher fetcher = this.getFetcher(uri, size);
		                
		                // Now do the fetch.
		                log.trace(format("Fetching: {0} {1} {2}", uri, size == null ? "-" : size, destinationPath));
		                fetcher.fetchFile(uri, size, destination, new MyContext());

		                // Finally, commit the file.
		                log.trace("Committing destination.");
		                BagFile committedFile = destination.commit();

	                    newBagFiles.add(committedFile); // synchronized

		                log.trace(format("Fetched: {0} -> {1}", uri, destinationPath));

		                target = getNextFetchItem();
	                }
	                catch (BagTransferCancelledException e)
	                {
	                	log.info("Transfer cancelled.");
	                	failedFetchTargets.add(target);
	                	result.addMessage("Transfer cancelled.");
	                	result.setSuccess(false);
	                	break;
	                }
	                catch (BagTransferException e)
	                {
	                    String msg = format("An error occurred while fetching target: {0}", target);
	                    log.warn(msg, e);
	                    
	                    if (destination != null)
	                    {
	                    	destination.abandon();
	                    	destination = null;
	                    }

	                    FetchFailureAction failureAction = failStrategy.registerFailure(target.getUrl(), target.getSize(), e);
	                    log.trace(format("Failure action for {0} (size: {1}): {2} ", target.getUrl(), target.getSize(), failureAction));
	                	
	                	if (failureAction == FetchFailureAction.RETRY_CURRENT)
	                	{
	                		// Do nothing.  The target variable will
	                		// remain the same, and we'll loop back around.
	                	}
	                	else if (failureAction == FetchFailureAction.CONTINUE_WITH_NEXT)
	                	{
	                		target = getNextFetchItem();
	                	}
	                	else // Default to STOP
	                	{
	                		// Stopping includes stopping all other thread
	                		// Make them finish up, too.
	                		BagFetcher.this.cancel();
	                		
		                    failedFetchTargets.add(target);		                    
		                    result.addMessage(msg);
		                    result.setSuccess(false);
		                    break;
	                	}
	                }
	            }
        	}
        	finally
        	{
        		this.closeFetchers();
        	}
            
            return result;
        }
        
        private synchronized FileFetcher getFetcher(URI uri, Long size) throws BagTransferException
        {
        	FileFetcher fetcher = this.fetchers.get(uri.getScheme());
        	
        	if (fetcher == null)
        	{
        		log.trace(format("Creating new FileFetcher for scheme: {0}", uri.getScheme()));
        		fetcher = newFileFetcher(uri, size);
        		
        		log.trace("Initializing new FileFetcher.");
        		fetcher.initialize();
        		
        		this.fetchers.put(uri.getScheme(), fetcher);
        	}
        	
        	return fetcher;
        }
        
        private synchronized void closeFetchers()
        {
        	for (FileFetcher fetcher : this.fetchers.values())
        	{
        		fetcher.close();
        	}
        	
        	this.fetchers.clear();
        }
    }
    
    private class MyContext implements FetchContext
    {
    	@Override
    	public boolean requiresLogin()
    	{
    		return false;
    	}

    	@Override
    	public PasswordAuthentication getCredentials()
    	{
    		return null;
    	}
    }
}
