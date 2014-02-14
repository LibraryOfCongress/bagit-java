package gov.loc.repository.bagit.transfer;

import static java.text.MessageFormat.format;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxt.FetchStatus;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.impl.FileBagFile;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.transformer.impl.UpdateCompleter;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.SimpleResultHelper;
import gov.loc.repository.bagit.verify.FailModeSupporting.FailMode;
import gov.loc.repository.bagit.verify.impl.ValidHoleyBagVerifier;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import java.io.File;
import java.io.InputStream;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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

import org.apache.commons.io.IOUtils;
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
public final class BagFetcher implements Cancellable, ProgressListenable{
    private static final Log log = LogFactory.getLog(BagFetcher.class);

    // Configurable from outside.
    private int numberOfThreads;
    private FetchFailStrategy failStrategy = StandardFailStrategies.FAIL_FAST;  // As per docs above.
    private FetchedFileDestinationFactory destinationFactory;
    private Map<String, FetchProtocol> protocolFactories = Collections.synchronizedMap(new HashMap<String, FetchProtocol>());
    private BagFactory bagFactory;
    private boolean isCancelled = false;
    private List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
    private String username;
    private String password;
    
    // Internal state.
    private Bag bagToFetch;
    private List<FetchTxt.FilenameSizeUrl> fetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>();
    private AtomicInteger nextFetchTargetIndex;
    private AtomicInteger fetchSuccessCounter = new AtomicInteger(0);
    private List<Fetcher> runningFetchers = Collections.synchronizedList(new ArrayList<Fetcher>());
    
    public BagFetcher(BagFactory bagFactory) {
    	this.bagFactory = bagFactory;
    	this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    
    @Override
    public void cancel(){
    	log.info("Cancelled.");
    	this.isCancelled = true;
    	
    	for (Fetcher fetcher : this.runningFetchers)
    	{
    		fetcher.cancel();
    	}
    }
    
    @Override
    public boolean isCancelled(){
    	return this.isCancelled;
    }
    
    @Override
    public void addProgressListener(ProgressListener progressListener){
    	this.progressListeners.add(progressListener);
    }
    
    @Override
    public void removeProgressListener(ProgressListener progressListener){
    	this.progressListeners.remove(progressListener);
    }
    
    private void progress(String activity, Object item, Long count, Long total){
    	for (ProgressListener listener : this.progressListeners){
    		listener.reportProgress(activity, item, count, total);
    	}
    }
    
    public int getNumberOfThreads(){
        return this.numberOfThreads;
    }
    
    public void setNumberOfThreads(int numberOfThreads){
        if (this.numberOfThreads < 1){
            throw new IllegalArgumentException(format("Number of threads cannot be less than 1: {0}", numberOfThreads));        	
        }
        
        this.numberOfThreads = numberOfThreads;
    }
    
    /**
     * Gets the current fetch failure strategy.
     * @return The currently set failure strategy.
     * 		   Will never be <code>null</code>.
     */
    public FetchFailStrategy getFetchFailStrategy(){
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
     * @param strategy The new strategy to use.  Cannot be <code>null</code>.
     * @throws NullPointerException Thrown if <code>null</code> is set.  
     */
    public void setFetchFailStrategy(FetchFailStrategy strategy) {
    	if (strategy == null)
    		throw new NullPointerException("strategy cannot be null");
    	
    	this.failStrategy = strategy;
    }
        
    public void setUsername(String username) {
		this.username = username;
	}

	protected String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	protected String getPassword() {
		return password;
	}

    public void registerProtocol(String scheme, FetchProtocol protocol){
        String normalizedScheme = scheme.toLowerCase();
        this.protocolFactories.put(normalizedScheme, protocol);
    }

    public SimpleResult fetch(Bag bag, FetchedFileDestinationFactory destinationFactory) throws BagTransferException {
        return this.fetch(bag, destinationFactory, false, false);
    }

    public SimpleResult fetch(Bag bag, FetchedFileDestinationFactory destinationFactory, boolean resume) throws BagTransferException {
        return this.fetch(bag, destinationFactory, resume, false);
    }
    
    /**
     * 
     * Fetch the payload files listed in the fetch.txt in a holey bag.
     * 
     * @param bag The holey bag to be filled.
     * @param destinationFactory Destination of the payload files once fetched.
     * @param resume The switch to fetch only missing and invalid files or all files listed in fetch.txt.
     * @param verify The switch to verify the holey bag before fetching any files.
     * @return SimpleResult indicates the fetch result with a list of fetch/verify-failure files. 
     * @throws BagTransferException If there is an error fetching the payload files
     */
    public SimpleResult fetch(Bag bag, FetchedFileDestinationFactory destinationFactory, boolean resume, boolean verify) throws BagTransferException {
        this.bagToFetch = bag;
        this.destinationFactory = destinationFactory;
        
        this.checkBagSanity();
        
        this.buildFetchTargets(resume, verify);
        this.nextFetchTargetIndex = new AtomicInteger(0);

        SimpleResult finalResult = new SimpleResult(true);
        
        if (this.numberOfThreads > 1){
            ExecutorService threadPool = Executors.newCachedThreadPool();
            
            BagFetcherShutdownHook shutdownHook = new BagFetcherShutdownHook();
            shutdownHook.hook();
            
            try {        
            	log.debug(format("Submitting {0} jobs.", this.numberOfThreads));
            	
                ArrayList<Future<SimpleResult>> futureResults = new ArrayList<Future<SimpleResult>>();      

            	for (int i = 0; i < this.numberOfThreads; i++) {
                	log.trace(format("Submitting job {0} of {1}.", i + 1, this.numberOfThreads));
                	Fetcher newFetcher = new Fetcher();
                	this.runningFetchers.add(newFetcher);
                    futureResults.add(threadPool.submit(newFetcher));
                }
                
            	log.debug("Jobs submitted.  Waiting on results.");
            	
                for (Future<SimpleResult> futureResult : futureResults){
                    try{
                        SimpleResult result = futureResult.get();
                        finalResult.merge(result);
                    }catch (ExecutionException e){
                        String msg = format("An unexpected exception occurred while processing the transfers: {0}", e.getCause().getMessage());
                        finalResult.addMessage(msg);
                        finalResult.setSuccess(false);
                        log.error(msg, e);
                    }catch (InterruptedException e){
                        String msg = format("Interrupted while waiting for the child threads to complete: {0}", e.getMessage());
                        finalResult.addMessage(msg);
                        finalResult.setSuccess(false);
                        log.error(msg, e);
                    }
                }
            }finally{
            	log.trace("Shutting down thread pool.");
                threadPool.shutdown();
            	log.trace("Shutting down thread pool.");
            	
            	log.trace("Releasing shutdown hook.");
            	shutdownHook.unhook();
            }
        } else {
        	log.debug("Fetching in single-threaded mode.");
        	
        	try{
	            Fetcher fetcher = new Fetcher();
	            SimpleResult result = fetcher.call();
	            finalResult.merge(result);
        	}catch (Exception e){
        		throw new BagTransferException("Caught unexpected exception from fetcher.", e);
        	}
        }
        
        log.debug(format("Fetch completed with result: {0}", finalResult.isSuccess()));
        progress("Fetch completed", "", null, null);
        
        if(finalResult.isSuccess()){
        	//Remove fetch-progress.txt when fetch is successful. 
        	this.deleteFetchProgressTxtOnDisk();

        	//Verify the bag to make sure that the return of success means the fetched bag is valid.
            progress("Verifing the fetched bag", "", null, null);
        	this.bagToFetch.loadFromManifests();
        	SimpleResult bagVerifyResult = this.bagToFetch.verifyValid(FailMode.FAIL_SLOW, this.progressListeners);
            log.debug(format("Verify valid completed with result: {0}", bagVerifyResult.isSuccess()));
            progress("Verify valid completed", "", null, null);
            return bagVerifyResult;
        }else{
            this.updateFetchProgressTxtOnDisk();        	
            return finalResult;
        }        
    }
    
    private void updateFetchProgressTxtOnDisk(){
    	//Reload bag is necessary when the bag is modified multiple times in a fill holey operation
    	this.bagToFetch.loadFromManifests();

    	FetchTxt fetchProgressTxt = this.bagToFetch.getFetchProgressTxt();
    	if(fetchProgressTxt == null){
    		fetchProgressTxt = this.bagFactory.getBagPartFactory().createFetchProgressTxt();
    		fetchProgressTxt.addAll(fetchLines);
    		this.bagToFetch.putBagFile(fetchProgressTxt);
    	}else if(fetchProgressTxt.size() <= 0){
    		fetchProgressTxt.addAll(fetchLines);    		
    	}else{
    		//Update the status of files in fetch-progress.txt
        	for(FilenameSizeUrl fetchLine : this.fetchLines){
            	int index = fetchProgressTxt.indexOf(fetchLine);
            	if(index >= 0){
                	fetchProgressTxt.get(index).setFetchStatus(fetchLine.getFetchStatus());        	            		
            	}
            }	
    	}    	
    	
        BagFactory bagFactory = new gov.loc.repository.bagit.BagFactory();
        UpdateCompleter completer = new UpdateCompleter(bagFactory);	
 		this.addProgressListeners(completer);
		
		//Payload files will not be updated by passing an empty lists to the following 
		completer.setLimitAddPayloadDirectories(new ArrayList<String>());
		completer.setLimitAddPayloadFilepaths(new ArrayList<String>());
		completer.setLimitDeletePayloadDirectories(new ArrayList<String>());
		completer.setLimitDeletePayloadFilepaths(new ArrayList<String>());
		completer.setLimitUpdatePayloadDirectories(new ArrayList<String>());
		completer.setLimitUpdatePayloadFilepaths(new ArrayList<String>());
		
		completer.setLimitAddTagDirectories(new ArrayList<String>());
		completer.setLimitAddTagFilepaths(new ArrayList<String>());
		completer.setLimitDeleteTagDirectories(new ArrayList<String>());
		completer.setLimitDeleteTagFilepaths(new ArrayList<String>());
		completer.setLimitUpdateTagDirectories(new ArrayList<String>());
		
		List<String> toUpdatedTagFilepaths = new ArrayList<String>();
		toUpdatedTagFilepaths.add(this.bagToFetch.getBagConstants().getFetchProgressTxt());
		completer.setLimitUpdateTagFilepaths(toUpdatedTagFilepaths);
		completer.complete(this.bagToFetch);		
		
		//Write the updated tag files on disk
		FileSystemWriter writer = new FileSystemWriter(bagFactory);
 		this.addProgressListeners(writer);		
		writer.setTagFilesOnly(true);
		this.bagToFetch.write(writer, this.bagToFetch.getFile());
    }
    
    private void deleteFetchProgressTxtOnDisk(){
    	log.info("Delete fetch-progress.txt.");
    	//Reload bag is necessary when the bag is modified multiple times in a fill holey operation
    	this.bagToFetch.loadFromManifests(); 
    	if(this.bagToFetch.getFetchProgressTxt() != null){
        	this.bagToFetch.removeBagFile(this.bagToFetch.getFetchProgressTxt().getFilepath());    		
    	}
    	
    	BagFactory bagFactory = new gov.loc.repository.bagit.BagFactory();
        UpdateCompleter completer = new UpdateCompleter(bagFactory);	
 		this.addProgressListeners(completer);

 		//Payload files will not be updated by passing an empty lists to the following 
 		completer.setLimitAddPayloadDirectories(new ArrayList<String>());
 		completer.setLimitAddPayloadFilepaths(new ArrayList<String>());
 		completer.setLimitDeletePayloadDirectories(new ArrayList<String>());
 		completer.setLimitDeletePayloadFilepaths(new ArrayList<String>());
 		completer.setLimitUpdatePayloadDirectories(new ArrayList<String>());
 		completer.setLimitUpdatePayloadFilepaths(new ArrayList<String>());
 		
 		completer.setLimitAddTagDirectories(new ArrayList<String>());
 		completer.setLimitAddTagFilepaths(new ArrayList<String>());
 		completer.setLimitDeleteTagDirectories(new ArrayList<String>());
 		completer.setLimitUpdateTagDirectories(new ArrayList<String>());
 		
 		List<String> toDeletededTagFilepaths = new ArrayList<String>();
 		toDeletededTagFilepaths.add(this.bagToFetch.getBagConstants().getFetchProgressTxt());
 		completer.setLimitDeleteTagFilepaths(toDeletededTagFilepaths);
 		
 		completer.complete(this.bagToFetch);		
 		
 		//Write the updated tag files on disk
 		FileSystemWriter writer = new FileSystemWriter(bagFactory);
 		this.addProgressListeners(writer);
 		writer.setTagFilesOnly(true);
 		writer.setFilesThatDoNotMatchManifestOnly(true);
 		this.bagToFetch.write(writer, this.bagToFetch.getFile());
    }
    
    private void addProgressListeners(ProgressListenable progressListenable){
    	for(ProgressListener progressListener : this.progressListeners){
    		progressListenable.addProgressListener(progressListener);
        }
    }
    
    private void checkBagSanity() throws BagTransferException{
    	log.debug("Checking sanity of bag prior to fetch.");
        progress("Checking sanity of bag prior to fetch", "", null, null);
        ValidHoleyBagVerifier verifier = new ValidHoleyBagVerifier();
        this.addProgressListeners(verifier);
    	SimpleResult verifyResult = this.bagToFetch.verify(verifier);
    	
    	if (!verifyResult.isSuccess()){
    		throw new BagTransferException(format("Bag is not valid: {0}", verifyResult.toString()));
    	}
    }
    
    private void recreateFetchProgressTxt(){
    	log.info("Recreate fetch-progress.txt.");
    	if(this.bagToFetch.getFetchProgressTxt() != null){
    		this.bagToFetch.getFetchProgressTxt().clear();
    	}
    	
    	List<FetchTxt.FilenameSizeUrl> allFetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>(this.bagToFetch.getFetchTxt());        		        	
        progress("Verifying the holey bag before fetching", "", null, null);
    	SimpleResult bagVerifyResult = this.bagToFetch.verifyValid(FailMode.FAIL_SLOW, this.progressListeners);
    	for(FetchTxt.FilenameSizeUrl fetchLine : allFetchLines){
    		if(BagHelper.isPayload(fetchLine.getFilename(), this.bagFactory.getBagConstants())){
    			if(SimpleResultHelper.isMissingPayloadFile(bagVerifyResult, fetchLine.getFilename())){
    				fetchLine.setFetchStatus(FetchTxt.FetchStatus.NOT_FETCHED);
    			}else if(SimpleResultHelper.isInvalidPayloadFile(bagVerifyResult, fetchLine.getFilename())){
    				fetchLine.setFetchStatus(FetchTxt.FetchStatus.VERIFY_FAILED);
    			}else{
    				fetchLine.setFetchStatus(FetchTxt.FetchStatus.SUCCEEDED);        				
    			}
				this.fetchLines.add(fetchLine);				         			
    		}			      
    	}      
    	//Update fetch-progress.txt.
    	this.updateFetchProgressTxtOnDisk();
    }
    
    public void buildFetchTargets(boolean resume, boolean verify){
        progress("Building fetch targets", "", null, null);
    	
    	log.trace("Getting fetch lines.");
        //Verify the bag to get the status of payload files. 
    	if(verify){
    		this.recreateFetchProgressTxt();
        }
        
        //If resume, only fetch these files whose status are not SUCCEEDED in fetch-progress.txt.  Otherwise, each file listed in fetch.txt will be fetched.
    	if(resume){
    		if(this.bagToFetch.getFetchProgressTxt() == null){
    			this.recreateFetchProgressTxt();
    		}
    		
    		List<FetchTxt.FilenameSizeUrl> allFetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>(this.bagToFetch.getFetchProgressTxt());        		        	
    		this.fetchLines.clear();
        	for(FetchTxt.FilenameSizeUrl fetchLine : allFetchLines){
        		if(fetchLine.getFetchStatus() == null || 
        		   ! fetchLine.getFetchStatus().equals(FetchTxt.FetchStatus.SUCCEEDED)){
        			this.fetchLines.add(fetchLine);
        		}
        	}        	
        }else{
    		List<FetchTxt.FilenameSizeUrl> allFetchLines = new ArrayList<FetchTxt.FilenameSizeUrl>(this.bagToFetch.getFetchTxt());        		        	
    		this.fetchLines.clear();
    		this.fetchLines.addAll(allFetchLines);        		        	
        }
    }
    
    private FilenameSizeUrl getNextFetchLine(){
        FilenameSizeUrl nextItem;
        
        int next = this.nextFetchTargetIndex.getAndIncrement();
        int size = this.fetchLines.size();

        if (next < size){
            nextItem = this.fetchLines.get(next);
            this.progress("starting fetch", nextItem.getFilename(), (long)next + 1, (long)size);
            log.trace(format("Fetching {0}/{1}: {2}", next + 1, size, nextItem.getFilename()));
        }else{
            nextItem = null;
            log.trace("Nothing left to fetch.  Returning null.");
        }
        
        return nextItem;
    }
    
    private FileFetcher newFileFetcher(URI uri, Long size) throws BagTransferException{
    	String scheme = uri.getScheme();

    	log.trace(format("Getting fetcher for scheme: {0}", scheme));
    	FetchProtocol factory = this.protocolFactories.get(scheme);
        
        if (factory == null){
            throw new BagTransferException(format("No registered factory for URI: {0}", uri));        	
        }
        
        return factory.createFetcher(uri, size);
    }
    
    private URI parseUri(String uriString) throws BagTransferException{
        try{
            return new URI(uriString);
        }catch (URISyntaxException e){
            String msg = format("Invalid target URL: {0}", uriString);
            log.error(msg, e);
            throw new BagTransferException(msg, e);
        }
    }
    
    /**
     * 
     * Fetch a bag hosted on a remote server.  Tag files will be fetched first.  
     * Fetch.txt will be generated based on manifest files if it is not available on the remote server.
     * Then the payload files will be fetched.
     * 
     * @param destFile The destination for the bag to fetch.
     * @param url The url of the remote bag.
     * @param resume The switch to fetch only missing and invalid files or all files listed in fetch.txt.
     * @param verify The switch to verify the holey bag before fetching any files.
     * @return SimpleResult indicates the fetch result with a list of fetch/verify-failure files. 
     * @throws BagTransferException If there is an error fetching the remote bag.
     */
    public SimpleResult fetchRemoteBag(File destFile, String url, boolean resume, boolean verify) throws BagTransferException{
        this.destinationFactory = new FileSystemFileDestination(destFile);

        log.info("Making local holey bag from remote bag");
		String baseUrl = url;
		if (! baseUrl.endsWith("/")) {
			baseUrl += "/";
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
		Bag partialBag = bagFactory.createBag(destFile, LoadOption.BY_MANIFESTS);
		//Should be a payload manifest or fetch.txt
		if (partialBag.getFetchTxt() == null && partialBag.getPayloadManifests().isEmpty()) {
			return new SimpleResult(false, "Neither fetch.txt or payload manifest found");
		}
		
		//Get tag manifests and write to disk
		for(Manifest manifest: partialBag.getTagManifests()){
			fetchFromManifest(manifest, partialBag.getBagConstants(), baseUrl);
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

		SimpleResult fetchResult = this.fetch(partialBag, dest, resume, verify);

    	return fetchResult;
    }

	protected SimpleResult fetchFromManifest(Manifest manifest, BagConstants bagConstants, String baseUrl) throws BagTransferException{
		SimpleResult result = new SimpleResult(true);
		
		for(String filepath : manifest.keySet()){
			result = fetchFile(baseUrl, filepath);
			if(! result.isSuccess()) {
				this.fail("File {0} in manifest {1} missing from bag.", filepath, manifest.getFilepath());
				return result;
			}
				
		}
		return result;
	}

	private void fail(String format, Object...args){
		this.fail(MessageFormat.format(format, args));
	}
	
	private void fail(String message){
		log.trace(message);
	}
	
    private SimpleResult fetchFile(String url, String filename){
    	SimpleResult result = new SimpleResult(true);
		Fetcher fetcher = new Fetcher();

		url += filename;
		
    	FilenameSizeUrl filenNameSizeUrl = new FetchTxt.FilenameSizeUrl(filename,null,url);
   		try{    		
    		fetcher.fetchFile(filenNameSizeUrl);
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
    	
		Fetcher fetcher = new Fetcher();

		//Fetch TagManifests
		for(Manifest.Algorithm algorithm: Manifest.Algorithm.values()){
			
			String filename = ManifestHelper.getTagManifestFilename(algorithm, bagConstants);
			FilenameSizeUrl filenNameSizeUrl = new FetchTxt.FilenameSizeUrl(filename,null,url+filename);
			try{    		
				fetcher.fetchFile(filenNameSizeUrl);
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
				fetcher.fetchFile(filenNameSizeUrl);
			} catch (BagTransferCancelledException bte){
				log.trace(format("Manifest file {0} does not exist in the remote bag",filename));
				
			} catch (BagTransferException bte){
				log.trace(format("Manifest file {0} does not exist in the remote bag",filename));
			}
		}
	}
    
	private class Fetcher implements Callable<SimpleResult> {
    	private SimpleResult result = new SimpleResult(true);
    	private Map<String, FileFetcher> fetchers = new HashMap<String, FileFetcher>();
    	
    	public synchronized void cancel(){
    		for (FileFetcher fetcher : this.fetchers.values()){
    			if (!fetcher.isCancelled()){
    				fetcher.cancel();    				
    			}
    		}
    	}
    	
        public SimpleResult call() {
        	log.trace("Internal fetcher started.");
        	
        	try{
        		FilenameSizeUrl fetchLine = getNextFetchLine();
        		
	            while (fetchLine != null && !isCancelled()){
	            	try{
						this.fetchFile(fetchLine);
						int index = fetchSuccessCounter.incrementAndGet();
                		progress("Fetched", fetchLine.getFilename(), new Long(index), new Long(fetchLines.size()));
						fetchLine = getNextFetchLine();
					}catch (BagTransferCancelledException e){
                		progress("Fetch cancelled", "", null, null);
						log.info("Transfer cancelled.");
	                	result.addMessage("Transfer cancelled.");
	                	result.setSuccess(false);
	            		break;
	            	}catch (BagTransferException e){
	                    FetchFailureAction failureAction = failStrategy.registerFailure(fetchLine, e);
	                    log.trace(format("Failure action for {0} (size: {1}): {2} ", fetchLine.getFilename(), fetchLine.getSize(), failureAction));
	    	                	
	                	if (failureAction == FetchFailureAction.RETRY_CURRENT){
	                		// Do nothing.  The target variable will
	                		// remain the same, and we'll loop back around.
	                	}else if (failureAction == FetchFailureAction.CONTINUE_WITH_NEXT){
	                		String errorMsg = null;
		                    if(fetchLine.getFetchStatus().equals(FetchStatus.FETCH_FAILED)){
		                    	errorMsg = format("An error occurred while fetching target: {0}", fetchLine.getFilename());
		                    	log.trace(errorMsg);
		                		result.addMessage(FetchStatus.FETCH_FAILED.toString(), "{0}: {1}", "fetch failed", fetchLine.getFilename());
		                	} else if(fetchLine.getFetchStatus().equals(FetchStatus.VERIFY_FAILED)){
		                    	errorMsg = format("The checksum of the fetched target {0} does not match that in the manifest.", fetchLine.getFilename());
		                    	log.trace(errorMsg);
		                		result.addMessage(FetchStatus.VERIFY_FAILED.toString(), "{0}: {1}", "verify failed", fetchLine.getFilename());
		                    }
		                    result.setSuccess(false);
		                    fetchLine = getNextFetchLine();
	                	}else { // Default to STOP
	                		// Stopping includes stopping all other thread
	                		// Make them finish up, too.
	                		BagFetcher.this.cancel();
	                		result.addMessage(format("An error occurred while fetching target: {0}", fetchLine.getFilename()));
		                    result.setSuccess(false);
		                    break;
	                	}
					}
	            }
	            	
        	}finally{
        		this.closeFetchers();
        	}
            
            return result;
        }
        
        private void fetchFile(FetchTxt.FilenameSizeUrl filenameSizeUrl) throws BagTransferException{
        	FetchedFileDestination destination = null;
        	BagFile committedFile = null;
        	
        	try{        				 
        		// The fetch.txt line parts.
                URI uri = parseUri(filenameSizeUrl.getUrl());
                Long size = filenameSizeUrl.getSize();
                String destinationPath = filenameSizeUrl.getFilename();

                // Create the destination for the file.
                log.trace(format("Creating destination: {0}", destinationPath));
                destination = destinationFactory.createDestination(destinationPath, size);

                // Create the object to do the fetching.
                FileFetcher fetcher = this.getFetcher(uri, size);
                
                // Now do the fetch.
        		progress("Fetching", filenameSizeUrl.getFilename(), null, null);		                			
                log.trace(format("Fetching: {0} {1} {2}", uri, size == null ? "-" : size, destinationPath));
                fetcher.fetchFile(uri, size, destination, new MyContext());

                // Finally, commit the file.
                log.trace("Committing destination.");
                committedFile = destination.commit();

                progress("Fetched", filenameSizeUrl.getFilename(), null, null);		                			
                  
                log.trace(format("Fetched: {0} -> {1}", uri, destinationPath));                
        	}catch (BagTransferCancelledException e){
        		throw new BagTransferCancelledException(e);
        	}catch (BagTransferException e){
                String msg = format("An error occurred while fetching target: {0}", filenameSizeUrl);
                log.warn(msg, e);
                
                filenameSizeUrl.setFetchStatus(FetchStatus.FETCH_FAILED);
                
                if (destination != null){
                	destination.abandon();
                	destination = null;
                }

                throw new BagTransferException(e);
        	}
        	
        	//After a file is fetched successfully, verify the checksum of the file against that in the manifest files.
        	if(bagToFetch != null && committedFile != null && committedFile.exists()){
        		progress("Verifying", filenameSizeUrl.getFilename(), null, null);		                			
        		InputStream stream = null;
        		try{
        			stream = committedFile.newInputStream();
                    boolean fixityMatches = false;
        			for(Manifest manifest : bagToFetch.getPayloadManifests()){
                    	if(MessageDigestHelper.fixityMatches(stream, manifest.getAlgorithm(), manifest.get(filenameSizeUrl.getFilename()))){
                    		filenameSizeUrl.setFetchStatus(FetchStatus.SUCCEEDED);
                            fixityMatches = true;
                            break;
                    	}
                    }
        			if(!fixityMatches){
        				filenameSizeUrl.setFetchStatus(FetchStatus.VERIFY_FAILED);
                        String msg = format("The checksum of the fetched target {0} does not match that in the manifest.", filenameSizeUrl);
                        log.warn(msg);
                        throw new BagTransferException(msg);
        			}
            		progress("Verification completed", filenameSizeUrl.getFilename(), null, null);		                			
        		}finally {
                	IOUtils.closeQuietly(stream);
                }        		
        	}
        }
        
        private synchronized FileFetcher getFetcher(URI uri, Long size) throws BagTransferException{
        	FileFetcher fetcher = this.fetchers.get(uri.getScheme());
        		
        	if (fetcher == null){
        		log.trace(format("Creating new FileFetcher for scheme: {0}", uri.getScheme()));
        		fetcher = newFileFetcher(uri, size);
        		
        		log.trace("Initializing new FileFetcher.");
        		fetcher.initialize();
        		
        		this.fetchers.put(uri.getScheme(), fetcher);
        	}
        	
        	if(username != null && password != null){
        		fetcher.setUsername(username);
        		fetcher.setPassword(password);
        	}
        	
        	return fetcher;
        }
        
        private synchronized void closeFetchers(){
        	for (FileFetcher fetcher : this.fetchers.values()){
        		fetcher.close();
        	}
        	
        	this.fetchers.clear();
        }
    }


    private class MyContext implements FetchContext{
    	@Override
    	public boolean requiresLogin(){
    		return false;
    	}

    	@Override
    	public PasswordAuthentication getCredentials(){
    		return null;
    	}
    }
    
    private class BagFetcherShutdownHook extends Thread{
        private CountDownLatch shutdownLatch;
        
        public synchronized void hook(){
        	this.shutdownLatch = new CountDownLatch(1);
        	Runtime.getRuntime().addShutdownHook(this);
        }
        
        public synchronized void unhook(){
        	this.shutdownLatch.countDown();       
        	
        	try{
        		Runtime.getRuntime().removeShutdownHook(this);
        	}catch (IllegalStateException e){
        		// Ignore this - we're already shutting down.
        		// http://java.sun.com/javase/6/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)
        	}
        }
        
    	@Override
    	public void run(){
    		cancel();
    		
    		try{
    			// Wait for a few seconds, so that the thread pool and
    			// fetchers can clean up a bit.  Then let things die.
				this.shutdownLatch.await(7, TimeUnit.SECONDS);
			}catch (InterruptedException e){
    			log.error("Timed out while waiting for fetch shutdown to finish.");
			}
    	}
    }
}
