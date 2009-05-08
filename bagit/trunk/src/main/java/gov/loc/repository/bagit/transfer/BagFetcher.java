package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.utilities.SimpleResult;

import static java.text.MessageFormat.format;

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
 * @author Brian Vargas
 * @version $Id$
 */
public class BagFetcher
{
    private static final Log log = LogFactory.getLog(BagFetcher.class);
    
    private Bag bagToFetch;
    private int numberOfThreads;
    private List<FetchTxt.FilenameSizeUrl> fetchTargets;
    private List<FetchTxt.FilenameSizeUrl> failedFetchTargets;
    private AtomicInteger nextFetchTargetIndex;
    private FetchedFileDestinationFactory destinationFactory;
    private Map<String, FetchProtocol> protocolFactories = Collections.synchronizedMap(new HashMap<String, FetchProtocol>());
    private List<BagFile> newBagFiles;
    private BagFactory bagFactory;
    
    public BagFetcher(BagFactory bagFactory) {
    	this.bagFactory = bagFactory;
    	this.numberOfThreads = Runtime.getRuntime().availableProcessors();
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
        
    public void registerProtocol(String scheme, FetchProtocol protocol)
    {
        String normalizedScheme = scheme.toLowerCase();
        this.protocolFactories.put(normalizedScheme, protocol);
    }
    
    public BagFetchResult fetch(Bag bag, FetchedFileDestinationFactory destinationFactory) throws BagTransferException
    {
        this.bagToFetch = bag;
        this.destinationFactory = destinationFactory;
        
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
                ArrayList<Future<SimpleResult>> futureResults = new ArrayList<Future<SimpleResult>>();
                
                for (int i = 0; i < this.numberOfThreads; i++)
                {
                    futureResults.add(threadPool.submit(new Fetcher()));
                }
                
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
                threadPool.shutdown();
            }
        }
        else
        {
            Fetcher fetcher = new Fetcher();
            SimpleResult result = fetcher.call();
            finalResult.merge(result);
        }
        
        // Clone the existing bag, and set it to be returned.
        Bag resultBag = this.bagFactory.createBag(bag);
        finalResult.setResultingBag(resultBag);

        // Add in the new bag files.
        resultBag.putBagFiles(this.newBagFiles);
        
        // And reset the fetch.txt, and add back any that failed.
        if (this.failedFetchTargets.size() > 0)
        {
            if (resultBag.getFetchTxt() == null)
            {
                resultBag.getBagPartFactory().createFetchTxt();
            }

            resultBag.getFetchTxt().clear();
            resultBag.getFetchTxt().addAll(this.failedFetchTargets);
        }
                
        return finalResult;
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
        
        if (next < this.fetchTargets.size())
            nextItem = this.fetchTargets.get(next);
        else
            nextItem = null;
        
        return nextItem;
    }
    
    private FileFetcher newFileFetcher(URI uri, Long size) throws BagTransferException
    {
        FetchProtocol factory = this.protocolFactories.get(uri.getScheme());
        
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
        public SimpleResult call()
        {
            FetchTxt.FilenameSizeUrl target = getNextFetchItem();
            
            while (target != null)
            {
                try
                {
                    BagFile fetchedFile = fetchTarget(target);
                    newBagFiles.add(fetchedFile); // synchronized
                }
                catch (BagTransferException e)
                {
                    failedFetchTargets.add(target);
                    
                    String msg = format("Unable to fetch target: {0}", target);
                    result.addMessage(msg);
                    result.setSuccess(false);
                    log.error(msg, e);
                }
                
                target = getNextFetchItem();
            }
            
            return result;
        }
        
        private SimpleResult result = new SimpleResult(true);
        
        private BagFile fetchTarget(FetchTxt.FilenameSizeUrl target) throws BagTransferException
        {
            URI uri = parseUri(target.getUrl());
            Long size = target.getSize();
            String destinationPath = target.getFilename();
            
            FileFetcher fetcher = newFileFetcher(uri, size);
            FetchedFileDestination destination = destinationFactory.createDestination(destinationPath, size);
            
            fetcher.fetchFile(uri, size, destination);
            
            log.trace("Committing destination.");
            BagFile committedFile = destination.commit();
            log.trace("Commit successful.");
            
            return committedFile;
        }
    }
}
