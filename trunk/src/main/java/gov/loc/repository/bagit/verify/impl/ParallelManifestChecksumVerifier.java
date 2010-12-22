package gov.loc.repository.bagit.verify.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.BagVerifyResult;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.ThreadSafeIteratorWrapper;
import gov.loc.repository.bagit.utilities.VFSHelper;
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
public class ParallelManifestChecksumVerifier extends LongRunningOperationBase implements ManifestChecksumVerifier
{
    private static final Log log = LogFactory.getLog(ParallelManifestChecksumVerifier.class);
    
    public ParallelManifestChecksumVerifier()
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
        
        log.debug(MessageFormat.format("Verifying manifests on {0} threads.", this.numberOfThreads));
        
        SimpleResult finalResult = new BagVerifyResult(true);
                
        int manifestCount = 0;
        int manifestTotal = manifests.size();
        for (final Manifest manifest : manifests)
        {
        	if (this.isCancelled()) return null;
        	
        	manifestCount++;
        	this.progress("verifying manifest checksums", manifest.getFilepath(), manifestCount, manifestTotal);
        	
        	final Manifest.Algorithm alg = manifest.getAlgorithm();
            final Iterator<String> manifestIterator = manifest.keySet().iterator();
            ArrayList<Future<BagVerifyResult>> futures = new ArrayList<Future<BagVerifyResult>>(this.numberOfThreads);
        	
            ExecutorService threadPool = Executors.newCachedThreadPool();
            try {
                final int fileTotal = manifest.size();
                final AtomicInteger fileCount = new AtomicInteger();
                for (int i = 0; i < this.numberOfThreads; i++)
                {            
                    Future<BagVerifyResult> future = threadPool.submit(new Callable<BagVerifyResult>() {
                        public BagVerifyResult call() {
                            ThreadSafeIteratorWrapper<String> safeIterator = new ThreadSafeIteratorWrapper<String>(manifestIterator);
                            BagVerifyResult result = new BagVerifyResult(true);
                            
                            for (String filePath : safeIterator)
                            {
                            	if (isCancelled()) return null;
                            	progress("verifying file checksum", filePath, fileCount.incrementAndGet(), fileTotal);
                            	if (log.isDebugEnabled())
                                    log.debug(MessageFormat.format("Verifying {1} fixity for file: {0}", filePath, alg.bagItAlgorithm));
                            	
                                BagFile file = bag.getBagFile(filePath);
                                
                                if (file != null && file.exists())
                                {
                                    String fixity = manifest.get(filePath);
                                    InputStream stream =  file.newInputStream();
                                    
                                    try
                                    {
	                                    if (!MessageDigestHelper.fixityMatches(stream, alg, fixity))
	                                    {
	                                        String msg = MessageFormat.format("Fixity failure in manifest {0}: {1}", manifest.getFilepath(), filePath);
	                                        log.debug(msg);
	                                        result.addMissingOrInvalidFile(filePath);
	                                        result.addMessage(msg);
	                                        result.setSuccess(false); 
	                                    }
                                    }
                                    finally
                                    {
                                    	IOUtils.closeQuietly(stream);
                                    }
                                }
                                else
                                {
                                    String msg = MessageFormat.format("File missing from manifest {0}: {1}", manifest.getFilepath(), filePath);
                                    log.debug(msg);
                                    result.addMissingOrInvalidFile(filePath);
                                    result.addMessage(msg);
                                    result.setSuccess(false);
                                }
                            }
                            //Close the FileSystemManager on this thread
                            VFSHelper.closeFileSystemManager();
                            return result;
                        }
                    });
                    
                    futures.add(future);
                }
            
                for (Future<BagVerifyResult> future : futures)
                {
                	SimpleResult futureResult;
                	
                	try
                	{
                		futureResult = future.get();
                	}
                	catch (ExecutionException e)
                	{
                		futureResult = new SimpleResult(false, e.getCause().getMessage());
                		log.error("An error occurred while processing the manifest.", e.getCause());
                	}
                    catch (InterruptedException e)
                    {
                        futureResult = new SimpleResult(false, "Execution was interrupted before completion.");
                        log.error("Execution was interrupted before completion.", e);
                    }

                    finalResult.merge(futureResult);
                }               
            } finally
            {
                log.debug("Shutting down thread pool.");
                threadPool.shutdown();
                log.debug("Thread pool shut down.");
            }

        }
                
    	if (this.isCancelled()) return null;
    	
    	return finalResult;
    }
    
    private int numberOfThreads;
    
}
