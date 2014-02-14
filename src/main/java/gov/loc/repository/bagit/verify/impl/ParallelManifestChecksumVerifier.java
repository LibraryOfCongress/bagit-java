package gov.loc.repository.bagit.verify.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.SimpleResultHelper;
import gov.loc.repository.bagit.utilities.ThreadSafeIteratorWrapper;
import gov.loc.repository.bagit.verify.FailModeSupporting;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.Verifier;

/**
 * A {@link Verifier} verification strategy that verifies a bag's payload
 * using multiple threads.  The number of threads is initially set to the
 * {@link Runtime#availableProcessors() number of CPUs}, but may be manually
 * set using {@link #setNumberOfThreads(int)}.
 * 
 */
public class ParallelManifestChecksumVerifier extends LongRunningOperationBase implements ManifestChecksumVerifier, FailModeSupporting
{
	private Normalizer.Form[] formArray = new Normalizer.Form[] { Normalizer.Form.NFC, Normalizer.Form.NFD };
    private static final Log log = LogFactory.getLog(ParallelManifestChecksumVerifier.class);
    
    private FailMode failMode = FailMode.FAIL_STAGE;
    
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
    public FailMode getFailMode() {
    	return this.failMode;
    }
    
    @Override
    public void setFailMode(FailMode failMode) {
    	this.failMode = failMode;    	
    }
    
    @Override
    public SimpleResult verify(final Manifest manifest, final Bag bag) {
    	List<Manifest> manifests = new ArrayList<Manifest>();
    	return this.verify(manifests, bag);
    }
    
    @Override
    public SimpleResult verify(final List<Manifest> manifests, final Bag bag) {
        
        log.debug(MessageFormat.format("Verifying manifests on {0} threads.", this.numberOfThreads));
        
        SimpleResult finalResult = new SimpleResult(true);
                
        int manifestCount = 0;
        int manifestTotal = manifests.size();

        for (final Manifest manifest : manifests)
        {
        	if (this.isCancelled()) return null;
        	
        	manifestCount++;
        	this.progress("verifying manifest checksums", manifest.getFilepath(), manifestCount, manifestTotal);
        	
        	final Manifest.Algorithm alg = manifest.getAlgorithm();
            final Iterator<String> manifestIterator = manifest.keySet().iterator();
            ArrayList<Future<SimpleResult>> futures = new ArrayList<Future<SimpleResult>>(this.numberOfThreads);
            final AtomicBoolean failFast = new AtomicBoolean(false);
        	
            ExecutorService threadPool = Executors.newCachedThreadPool();
            try {
                final int fileTotal = manifest.size();
                final AtomicInteger fileCount = new AtomicInteger();
                for (int i = 0; i < this.numberOfThreads; i++)
                {            
                    Future<SimpleResult> future = threadPool.submit(new Callable<SimpleResult>() {
                        public SimpleResult call() {
                            ThreadSafeIteratorWrapper<String> safeIterator = new ThreadSafeIteratorWrapper<String>(manifestIterator);
                            SimpleResult result = new SimpleResult(true);
                            
                            for (String filePath : safeIterator)
                            {
                            	if (isCancelled()) return null;
                            	if (FailMode.FAIL_FAST == failMode && failFast.get()) return result;
                            	progress("verifying file checksum", filePath, fileCount.incrementAndGet(), fileTotal);
                            	if (log.isDebugEnabled())
                                    log.debug(MessageFormat.format("Verifying {1} fixity for file: {0}", filePath, alg.bagItAlgorithm));
                            	
                            	boolean file_exists = false;
                            	String normalizedPath = null;
                            	BagFile file = null;
                            	for (Normalizer.Form form : formArray) {
                            		normalizedPath = Normalizer.normalize(filePath, form);
                            		file = bag.getBagFile(normalizedPath);
                            		if( file != null && file.exists()) {
                            			file_exists = true;
                            			break;
                            		}
                            	}
                            
                                if (file_exists)
                                {
                                	log.trace("Found file with path " + normalizedPath);
                                	String fixity = manifest.get(normalizedPath);
                                    InputStream stream = null;
                                    
                                    try
                                    {
                                    	log.trace("Getting input stream from " + file.getFilepath());
                                    	stream = file.newInputStream();
	                                    if (!MessageDigestHelper.fixityMatches(stream, alg, fixity))
	                                    {
	                                    	if (manifest.isPayloadManifest()) {
	                                    		SimpleResultHelper.invalidPayloadFile(result, manifest.getFilepath(), normalizedPath);
	                                    	} else {
	                                    		SimpleResultHelper.invalidTagFile(result, manifest.getFilepath(), normalizedPath);
	                                    	}
	                                        String msg = MessageFormat.format("Fixity failure in manifest {0}: {1}", manifest.getFilepath(), normalizedPath);
	                                        log.debug(msg);	                                        
	                                        failFast.set(true);
	                                    }
                                    } catch(RuntimeException ex) {
                                    	throw new RuntimeException(MessageFormat.format("Error checking fixity of {0}: {1}", filePath, ex.getMessage()), ex);
                                    }
                                    finally
                                    {
                                    	IOUtils.closeQuietly(stream);
                                    }
                                }
                                else
                                {
                                	if (manifest.isPayloadManifest()) {
                                		SimpleResultHelper.missingPayloadFile(result, manifest.getFilepath(), normalizedPath);
                                	} else {
                                		SimpleResultHelper.missingTagFile(result, manifest.getFilepath(), normalizedPath);
                            		}
                                    String msg = MessageFormat.format("File missing from manifest {0}: {1}", manifest.getFilepath(), normalizedPath);
                                    log.debug(msg);
                                    failFast.set(true);
                                }
                            }
                            return result;
                        }
                    });
                    
                    futures.add(future);
                }
            
                for (Future<SimpleResult> future : futures)
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
