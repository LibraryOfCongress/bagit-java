package gov.loc.repository.bagit.verify.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ProgressIndicator;
import gov.loc.repository.bagit.ProgressMonitorable;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.Verifier;

/**
 * A {@link Verifier verification strategy} that verifies a bag's payload
 * using multiple threads.  The number of threads is initially set to the
 * {@link Runtime#availableProcessors() number of CPUs}, but may be manually
 * set using {@link #setNumebrOfThreads(int)}.
 * 
 * @version $Id$
 */
public class ParallelManifestChecksumVerifier implements ManifestChecksumVerifier, Cancellable, ProgressMonitorable
{
    private static final Log log = LogFactory.getLog(ParallelManifestChecksumVerifier.class);
    
    private CancelIndicator cancelIndicator;
    private ProgressIndicator progressIndicator;
    
    public ParallelManifestChecksumVerifier()
    {
        //this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    	this.numberOfThreads = 1;
    	//TODO:  Temporarily setting this to 1 because > 1 causes problems for serialized bags
    }
    
    @Override
    public void setCancelIndicator(CancelIndicator cancelIndicator) {
    	this.cancelIndicator = cancelIndicator;
    	
    }
    
    @Override
    public void setProgressIndicator(ProgressIndicator progressIndicator) {
    	this.progressIndicator = progressIndicator;    	
    }
    
    public int getNumberOfThreads()
    {
        return this.numberOfThreads;
    }
    
    /**
     * Sets the number of threads to use in processing.
     * 
     * @param num The number of threads.  Cannot be less than 1.
     * @throws IllegalArgumentException Thrown if <tt>num</tt> is less than 1.
     */
    public void setNumberOfThreads(int num)
    {
        if (num < 1)
            throw new IllegalArgumentException("Number of threads must be at least 1.");
        
        this.numberOfThreads = num;
    }
    
    @Override
    public SimpleResult verify(final Manifest manifest, final Bag bag) {
    	List<Manifest> manifests = new ArrayList<Manifest>();
    	return this.verify(manifests, bag);
    }
    
    @Override
    public SimpleResult verify(final List<Manifest> manifests, final Bag bag) {

    	ExecutorService threadPool = Executors.newCachedThreadPool();
        
        log.debug(MessageFormat.format("Verifying manifests on {0} threads.", this.numberOfThreads));
        
        SimpleResult finalResult;
        
        try
        {
            // Initialize finalResult here, so that the compiler will check that
            // it is properly set to something elsewhere by all the catch
            // blocks.
            finalResult = new SimpleResult(true);
            
            int manifestCount = 0;
            int manifestTotal = manifests.size();
            for (final Manifest manifest : manifests)
            {
            	if (this.cancelIndicator != null && this.cancelIndicator.performCancel()) return null;
            	manifestCount++;
            	if (this.progressIndicator != null) this.progressIndicator.reportProgress("verifying manifest checksums", manifest.getFilepath(), manifestCount, manifestTotal);
            	final Manifest.Algorithm alg = manifest.getAlgorithm();
                final Iterator<String> manifestIterator = manifest.keySet().iterator();
                ArrayList<Future<SimpleResult>> futures = new ArrayList<Future<SimpleResult>>(this.numberOfThreads);

                final int fileTotal = manifest.size();
                for (int i = 0; i < this.numberOfThreads; i++)
                {            
                    Future<SimpleResult> future = threadPool.submit(new Callable<SimpleResult>() {
                        public SimpleResult call() {
                            ThreadSafeIteratorWrapper<String> safeIterator = new ThreadSafeIteratorWrapper<String>(manifestIterator);
                            SimpleResult result = new SimpleResult(true);
                            
                            for (String filePath : safeIterator)
                            {
                            	if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
                            	if (progressIndicator != null) progressIndicator.reportProgress("verifying file checksum", filePath, safeIterator.count(), fileTotal);
                            	if (log.isDebugEnabled())
                                    log.debug(MessageFormat.format("Verifying {1} fixity for file: {0}", filePath, alg.bagItAlgorithm));
                                BagFile file = bag.getBagFile(filePath);
                                
                                if (file != null && file.exists())
                                {
                                    String fixity = manifest.get(filePath);
                                    InputStream stream =  file.newInputStream();
                                    
                                    if (!MessageDigestHelper.fixityMatches(stream, alg, fixity))
                                    {
                                        String msg = MessageFormat.format("Fixity failure in manifest {0}: {1}", manifest.getFilepath(), filePath);
                                        log.debug(msg);
                                        result.addMessage(msg);
                                        result.setSuccess(false); 
                                    }
                                }
                                else
                                {
                                    String msg = MessageFormat.format("File missing from manifest {0}: {1}", manifest.getFilepath(), filePath);
                                    log.debug(msg);
                                    result.addMessage(msg);
                                    result.setSuccess(false);
                                }
                            }
                            
                            return result;
                        }
                    });
                    
                    futures.add(future);
                }
            
                for (Future<SimpleResult> future : futures)
                {
                    SimpleResult futureResult = future.get();
                    finalResult.merge(futureResult);
                }               
                
            }
        }
        catch (InterruptedException e)
        {
            log.error("Execution was interrupted before completion.", e);
            finalResult = new SimpleResult(false, "Execution was interrupted before completion.");
        }
        catch (ExecutionException e)
        {
            log.error("Execution threw an exception.", e);
            String msg = MessageFormat.format("Execution threw an exception: {0}", e.getCause().getMessage());
            finalResult = new SimpleResult(false, msg);
        }
        finally
        {
            log.debug("Shutting down thread pool.");
            threadPool.shutdown();
            log.debug("Thread pool shut down.");
        }
    	if (this.cancelIndicator != null && this.cancelIndicator.performCancel()) return null;        
    	return finalResult;
    }
    
    private int numberOfThreads;
    
    private static class ThreadSafeIteratorWrapper<E> implements Iterator<E>, Iterable<E>
    {
        private volatile int count = 0;
        
    	public ThreadSafeIteratorWrapper(Iterator<E> iterator)
        {
            this.iterator = iterator;
        }
        
        @Override
        public Iterator<E> iterator()
        {
            return this;
        }
        
        @Override
        public boolean hasNext()
        {
            synchronized (this)
            {
                synchronized (this.iterator)
                {
                    boolean hasNext = this.iterator.hasNext();
                    
                    if (hasNext)
                        this.nextItem = this.iterator.next();
                    else
                        this.nextItem = null;
                    
                    return hasNext;
                }
            }
        }

        @Override
        public E next()
        {
            synchronized (this)
            {
                if (this.nextItem == null)
                    throw new NoSuchElementException();
                count++;
                E tmp = this.nextItem;
                this.nextItem = null;
                return tmp;
            }
        }
        
        public int count() {
        	return this.count;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
        private E nextItem;
        private Iterator<E> iterator;
    }
}
