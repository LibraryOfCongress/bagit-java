package gov.loc.repository.bagit.verify;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.VerifyStrategy;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;

/**
 * A {@link VerifyStrategy verification strategy} that verifies a bag's payload
 * using multiple threads.  The number of threads is initially set to the
 * {@link Runtime#availableProcessors() number of CPUs}, but may be manually
 * set using {@link #setNumebrOfThreads(int)}.
 * 
 * @version $Id$
 */
public class ParallelPayloadStrategy implements VerifyStrategy
{
    private static final Log log = LogFactory.getLog(ParallelPayloadStrategy.class);
    
    public ParallelPayloadStrategy()
    {
        this.numberOfThreads = Runtime.getRuntime().availableProcessors();
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
    public void setNumebrOfThreads(int num)
    {
        if (num < 1)
            throw new IllegalArgumentException("Number of threads must be at least 1.");
        
        this.numberOfThreads = num;
    }
    
    @Override
    public SimpleResult verify(final Bag bag)
    {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        
        log.debug(MessageFormat.format("Verifying payload on {0} threads.", this.numberOfThreads));
        
        SimpleResult finalResult;
        
        try
        {
            // Initialize finalResult here, so that the compiler will check that
            // it is properly set to something elsewhere by all the catch
            // blocks.
            finalResult = new SimpleResult(true);
            
            for (final Manifest manifest : bag.getPayloadManifests())
            {
                final Manifest.Algorithm alg = manifest.getAlgorithm();
                final Iterator<String> manifestIterator = manifest.keySet().iterator();
                ArrayList<Future<SimpleResult>> futures = new ArrayList<Future<SimpleResult>>(this.numberOfThreads);
                
                for (int i = 0; i < this.numberOfThreads; i++)
                {            
                    Future<SimpleResult> future = threadPool.submit(new Callable<SimpleResult>() {
                        public SimpleResult call() {
                            ThreadSafeIteratorWrapper<String> safeIterator = new ThreadSafeIteratorWrapper<String>(manifestIterator);
                            SimpleResult result = new SimpleResult(true);
                            
                            for (String filePath : safeIterator)
                            {
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
        
        return finalResult;
    }
    
    private int numberOfThreads;
    
    private static class ThreadSafeIteratorWrapper<E> implements Iterator<E>, Iterable<E>
    {
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
                
                E tmp = this.nextItem;
                this.nextItem = null;
                return tmp;
            }
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
