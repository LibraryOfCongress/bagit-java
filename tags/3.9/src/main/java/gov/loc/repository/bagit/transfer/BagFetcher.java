package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;
import gov.loc.repository.bagit.impl.FileBagFile;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.utilities.BagVerifyResult;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.impl.ValidHoleyBagVerifier;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import static java.text.MessageFormat.format;

import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
    private List<FetchTarget> fetchTargets;
    private List<FetchTarget> failedFetchTargets;
    private AtomicInteger nextFetchTargetIndex;
    private List<BagFile> newBagFiles;
    private List<Fetcher> runningFetchers = Collections.synchronizedList(new ArrayList<Fetcher>());
    private String baseUrl;
    
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
        return this.fetch(bag, destinationFactory, false);
    }

    public BagFetchResult fetch(Bag bag, FetchedFileDestinationFactory destinationFactory, boolean resume) throws BagTransferException
    {
        this.bagToFetch = bag;
        this.destinationFactory = destinationFactory;
        
        this.checkBagSanity();
        
        this.buildFetchTargets(resume);
        this.nextFetchTargetIndex = new AtomicInteger(0);
        
        // Parts of the new bag.
        this.newBagFiles = Collections.synchronizedList(new ArrayList<BagFile>(this.fetchTargets.size()));
        this.failedFetchTargets = Collections.synchronizedList(new ArrayList<FetchTarget>());

        BagFetchResult finalResult = new BagFetchResult(true);
        
        if (this.numberOfThreads > 1)
        {
            ExecutorService threadPool = Executors.newCachedThreadPool();
            
            BagFetcherShutdownHook shutdownHook = new BagFetcherShutdownHook();
            shutdownHook.hook();
            
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
            	
            	log.trace("Releasing shutdown hook.");
            	shutdownHook.unhook();
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
            
            for (FetchTarget failedTarget : this.failedFetchTargets)
            {
            	resultBag.getFetchTxt().addAll(failedTarget.getLines());
            }
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
    		throw new BagTransferException(format("Bag is not valid: {0}", verifyResult.toString()));
    	}
    }
    
    private void buildFetchTargets(boolean resume)
    {
        // If resume is true, verify the bagToFetch to get a list of missing and corrupted files.
    	BagVerifyResult bagVerifyResult = null;
        if(resume){
			bagVerifyResult = this.bagToFetch.verifyValidFailSlow();
		}
        
    	// Retrieve the fetch items into a separate list, and then sort the
        // list by file name.  We want to group all the lines for the same file
    	// together.
    	log.trace("Sorting fetch lines by file name.");
    	ArrayList<FetchTxt.FilenameSizeUrl> sortedFetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>(this.bagToFetch.getFetchTxt());
    	Collections.sort(sortedFetchLines, new FetchFilenameSorter());

    	this.fetchTargets = new ArrayList<FetchTarget>(sortedFetchLines.size());
    	
        // Now reduce our fetch lines into FetchTarget instances.  This should be easy,
    	// since they're grouped already.
    	log.trace("Converting fetch lines into fetch targets.");
    	FetchTarget currentTarget = null;
    	
    	for (FetchTxt.FilenameSizeUrl line : sortedFetchLines)
    	{
    		// Do not add a file to the fetch targets if the file is not missing or corrupted.
    		if(resume && BagHelper.isPayload(line.getFilename(), this.bagFactory.getBagConstants()) && !bagVerifyResult.getMissingAndInvalidFiles().contains(line.getFilename())){
    			continue;
    		}else {
    			if (currentTarget == null || !currentTarget.getFilename().equals(line.getFilename()))
        		{
        			currentTarget = new FetchTarget(line);
        			this.fetchTargets.add(currentTarget);
        		}
        		else
        		{
        			currentTarget.addLine(line);
        		}
    		}
    	}
    	
    	// Now sort the targets by descending size.  We want to transfer the big, hard
        // stuff first.  Also, this will help make sure that threads get
        // evenly loaded. 
        log.trace(format("Sorting fetch target list with {0} items by size.", this.fetchTargets.size()));
        
        Collections.sort(fetchTargets, new Comparator<FetchTarget>() {
        	@Override
        	public int compare(FetchTarget left, FetchTarget right)
        	{
                Long leftSize = left.getSize();
                Long rightSize = right.getSize();
                int result;
                
                if (leftSize == null)
                {
                    if (rightSize == null)
                        result = 0;
                    else
                        result = -1;
                }
                else
                {
                    if (rightSize == null)
                        result = 1;
                    else
                        result = leftSize.compareTo(rightSize);
                }
                
                return result;
        	}
        });
        
        log.trace("Sort complete.");
    }
    
    private FetchTarget getNextFetchTarget()
    {
        FetchTarget nextItem;
        
        int next = this.nextFetchTargetIndex.getAndIncrement();
        int size = this.fetchTargets.size();
        
        
        if (next < size)
        {
            nextItem = this.fetchTargets.get(next);
            log.trace(format("Fetching {0}/{1}: {2}", next + 1, size, nextItem.getFilename()));
            this.progress("starting fetch", nextItem.getFilename(), (long)next + 1, (long)size);
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
    
    public SimpleResult fetchRemoteBag(File destFile, String url, boolean resume) throws BagTransferException{
        this.newBagFiles = Collections.synchronizedList(new ArrayList<BagFile>());
        this.destinationFactory = new FileSystemFileDestination(destFile);

        log.info("Making local holey bag from remote bag");
		this.baseUrl = url;
		if (! this.baseUrl.endsWith("/")) {
			this.baseUrl += "/";
		}

		//Fetch "bagit.txt" and write to disk
		SimpleResult bagItResult = fetchFile(baseUrl, bagFactory.getBagConstants().getBagItTxt());
		if (! bagItResult.isSuccess()){
			log.info("Failed: BagIt.txt file does not exist on remote bag");
			return bagItResult;
		} 
				
		//Read bagit.txt to get version
		String bagItTxtFilepath = destinationFactory.createDestination(bagFactory.getBagConstants().getBagItTxt(), null).getDirectAccessPath();
		BagItTxt bagItTxt = bagFactory.getBagPartFactory().createBagItTxt(new FileBagFile(bagFactory.getBagConstants().getBagItTxt(), new File(bagItTxtFilepath)));

		BagConstants bagConstants = bagFactory.getBagConstants(Version.valueOfString(bagItTxt.getVersion()));
		
		//Get the manifests and write to disk
		fetchManifestFiles(baseUrl, bagConstants);
		
		if(!resume){
			//Get fetch.txt (may exist) and write to disk
			fetchFile(baseUrl, bagConstants.getFetchTxt());
		}
			
		//Read bag from disk
		Bag partialBag = bagFactory.createBag(destFile, LoadOption.BY_PAYLOAD_MANIFESTS);
		//Should be a payload manifest or fetch.txt
		if (partialBag.getFetchTxt() == null && partialBag.getPayloadManifests().isEmpty()) {
			return new SimpleResult(false, "Neither fetch.txt or payload manifest found");
		}
		
		//Get tag manifests and write to disk
		for(Manifest manifest: partialBag.getTagManifests()){
			fetchFromManifest(manifest, partialBag.getBagConstants());
		}
		
		//Get bag-info.txt and write to disk		
		fetchFile(baseUrl, bagConstants.getBagInfoTxt());
		
		//If there is no fetch.txt on the remote server, generate a fetch.txt file 
		if(partialBag.getFetchTxt() == null){
			//Generate a fetch.txt and add it to the partial bag and the holey bag
			Bag holeyBag = partialBag.makeHoley(baseUrl, true, false, false);
			//Write the fetch.txt to disk
			holeyBag.write(new FileSystemWriter(bagFactory), destFile);			
		}
		
		FileSystemFileDestination dest = new FileSystemFileDestination(destFile);	    

		BagFetchResult fetchResult = this.fetch(partialBag, dest, resume);

		if (! fetchResult.isSuccess()) {
	    	return fetchResult;
	    }

		return new SimpleResult(true);
    }

	protected SimpleResult fetchFromManifest(Manifest manifest, BagConstants bagConstants) throws BagTransferException
	{
		SimpleResult result = new SimpleResult(true);
		
		for(String filepath : manifest.keySet()) 
		{
			result = fetchFile(baseUrl, filepath);
			if(! result.isSuccess()) {
				this.fail("File {0} in manifest {1} missing from bag.", filepath, manifest.getFilepath());
				return result;
			}
				
		}
		return result;
	}

	private void fail(String format, Object...args)
	{
		this.fail(MessageFormat.format(format, args));
	}
	
	private void fail(String message)
	{
		log.trace(message);
	}
	
    private SimpleResult fetchFile(String url, String filename){
    	SimpleResult result = new SimpleResult(true);
		Fetcher fetcher = new Fetcher();

		url += filename;
		
    	FilenameSizeUrl filenNameSizeUrl = new FetchTxt.FilenameSizeUrl(filename,null,url);
   		try{    		
    		fetcher.fetchSingleLine(filenNameSizeUrl);
		} catch (BagTransferCancelledException bte){
			log.trace(format("File {0} does not exist in the remote bag",filename));
			result.setSuccess(false);
		} catch (BagTransferException bte){
			log.trace(format("File {0} does not exist in the remote bag",filename));
			result.setSuccess(false);
		}
		return result;
	}

    private void fetchManifestFiles(String url, BagConstants bagConstants) throws BagTransferException{
    	
    	//SimpleResult result = new SimpleResult(false);
		Fetcher fetcher = new Fetcher();

		//Fetch TagManifests
		for(Manifest.Algorithm algorithm: Manifest.Algorithm.values()){
			
			String filename = ManifestHelper.getTagManifestFilename(algorithm, bagConstants);
			FilenameSizeUrl filenNameSizeUrl = new FetchTxt.FilenameSizeUrl(filename,null,url+filename);
			try{    		
				fetcher.fetchSingleLine(filenNameSizeUrl);
			} catch (BagTransferCancelledException bte){
				log.trace(format("Manifest file {0} does not exist in the remote bag",filename));
			} catch (BagTransferException bte){
				log.trace(format("Manifest file {0} does not exist in the remote bag",filename));
			}
		}
				
		//Fetch PayloadManifests
		for(Manifest.Algorithm algorithm: Manifest.Algorithm.values()){
			String filename = ManifestHelper.getPayloadManifestFilename(algorithm, bagConstants);
			FilenameSizeUrl filenNameSizeUrl = new FetchTxt.FilenameSizeUrl(filename,null,url+filename);
			try{    		
				fetcher.fetchSingleLine(filenNameSizeUrl);
			} catch (BagTransferCancelledException bte){
				log.trace(format("Manifest file {0} does not exist in the remote bag",filename));
				
			} catch (BagTransferException bte){
				log.trace(format("Manifest file {0} does not exist in the remote bag",filename));
			}
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
        		FetchTarget fetchTarget = getNextFetchTarget();
        		
	            while (fetchTarget != null && !isCancelled())
	            {
	            	try
					{
						this.fetchSingleTarget(fetchTarget);
						fetchTarget = getNextFetchTarget();
					}
	            	catch (BagTransferCancelledException e)
	            	{
	                	log.info("Transfer cancelled.");
	                	result.addMessage("Transfer cancelled.");
	                	result.setSuccess(false);
	            		break;
	            	}
	            	catch (BagTransferException e)
					{
	                    FetchFailureAction failureAction = failStrategy.registerFailure(fetchTarget, e);
	                    log.trace(format("Failure action for {0} (size: {1}): {2} ", fetchTarget.getFilename(), fetchTarget.getSize(), failureAction));
	    	                	
	                	if (failureAction == FetchFailureAction.RETRY_CURRENT)
	                	{
	                		// Do nothing.  The target variable will
	                		// remain the same, and we'll loop back around.
	                	}
	                	else if (failureAction == FetchFailureAction.CONTINUE_WITH_NEXT)
	                	{
		                    failedFetchTargets.add(fetchTarget);		                    
		                    result.addMessage(format("An error occurred while fetching target: {0}", fetchTarget.getFilename()));
		                    result.setSuccess(false);
	                		fetchTarget = getNextFetchTarget();
	                	}
	                	else // Default to STOP
	                	{
	                		// Stopping includes stopping all other thread
	                		// Make them finish up, too.
	                		BagFetcher.this.cancel();
	                		
		                    failedFetchTargets.add(fetchTarget);		                    
		                    result.addMessage(format("An error occurred while fetching target: {0}", fetchTarget.getFilename()));
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
        
        private void fetchSingleTarget(FetchTarget target) throws BagTransferException
        {
        	List<FetchTxt.FilenameSizeUrl> lines = target.getLines();
        	
        	// If there is more than one possible URL to pull from,
        	// then loop over them until they all fail.
        	if (lines.size() == 1)
        	{
        		FetchTxt.FilenameSizeUrl line = lines.get(0);
        		log.debug(format("Fetching single line: {0}", line));
        		this.fetchSingleLine(line);
        	}
        	else
        	{
        		for (int i = 0; i < lines.size() && !isCancelled(); i++)
        		{
        			try
        			{
        				FetchTxt.FilenameSizeUrl line = lines.get(i);
        				this.fetchSingleLine(line);
        				
            			// If we successfully fetch from that single line,
            			// then just break out of the loop.  We don't need
            			// to try transferring from somewhere else.
        				break;
        			}
        			catch (BagTransferCancelledException e)
        			{
                    	failedFetchTargets.add(target);
                    	throw new BagTransferCancelledException(e);
        			}
        			catch (BagTransferException e)
        			{
        				// If there are more URL lines to try, then keep going without
        				// error.  Otherwise, add this target to the list of failed targets
        				// and give up.
        				if (i + 1 < lines.size())
        				{
        					continue;
        				}
        				else
        				{
                        	failedFetchTargets.add(target);
                        	throw new BagTransferException(e);
        				}
        			}
        		}
        	}
        }
        
        private void fetchSingleLine(FetchTxt.FilenameSizeUrl fetchLine) throws BagTransferException
        {
        	FetchedFileDestination destination = null;
        	
        	try
        	{        				 
        		// The fetch.txt line parts.
                URI uri = parseUri(fetchLine.getUrl());
                Long size = fetchLine.getSize();
                String destinationPath = fetchLine.getFilename();

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
        	}
        	catch (BagTransferCancelledException e)
        	{
        		throw new BagTransferCancelledException(e);
        	}
        	catch (BagTransferException e)
        	{
                String msg = format("An error occurred while fetching target: {0}", fetchLine);
                log.warn(msg, e);
                
                if (destination != null)
                {
                	destination.abandon();
                	destination = null;
                }

                throw new BagTransferException(e);
        	}
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
    
    private class BagFetcherShutdownHook extends Thread
    {
        private CountDownLatch shutdownLatch;
        
        public synchronized void hook()
        {
        	this.shutdownLatch = new CountDownLatch(1);
        	Runtime.getRuntime().addShutdownHook(this);
        }
        
        public synchronized void unhook()
        {
        	this.shutdownLatch.countDown();       
        	
        	try
        	{
        		Runtime.getRuntime().removeShutdownHook(this);
        	}
        	catch (IllegalStateException e)
        	{
        		// Ignore this - we're already shutting down.
        		// http://java.sun.com/javase/6/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)
        	}
        }
        
    	@Override
    	public void run()
    	{
    		cancel();
    		
    		try
			{
    			// Wait for a few seconds, so that the thread pool and
    			// fetchers can clean up a bit.  Then let things die.
				this.shutdownLatch.await(7, TimeUnit.SECONDS);
			}
    		catch (InterruptedException e)
			{
    			log.error("Timed out while waiting for fetch shutdown to finish.");
			}
    	}
    }


}
