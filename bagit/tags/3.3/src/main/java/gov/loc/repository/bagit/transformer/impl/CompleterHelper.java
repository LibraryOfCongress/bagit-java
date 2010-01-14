package gov.loc.repository.bagit.transformer.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.ThreadSafeIteratorWrapper;
import gov.loc.repository.bagit.utilities.VFSHelper;

public class CompleterHelper extends LongRunningOperationBase {
	
    private static final Log log = LogFactory.getLog(CompleterHelper.class);

	private int numberOfThreads = 1;
	
	public CompleterHelper() {
		this.numberOfThreads = Runtime.getRuntime().availableProcessors();
	}
	
    public void setNumberOfThreads(int num) {
        if (num < 1)
            throw new IllegalArgumentException("Number of threads must be at least 1.");
        
        this.numberOfThreads = num;
    }
		
	public void clearManifests(Bag bag, Collection<Manifest> manifests) {
		for(Manifest manifest : manifests) {
			bag.removeBagFile(manifest.getFilepath());
		}
	}
	
	public void cleanManifests(Bag bag, Collection<Manifest> manifests) {
		int manifestTotal = manifests.size();
		int manifestCount = 0;
		for(Manifest manifest : manifests) {			
			manifestCount++;
			
			this.progress("cleaning manifest", manifest.getFilepath(), manifestCount, manifestTotal);
			
			List<String> deleteFilepaths = new ArrayList<String>();
			for(String filepath : manifest.keySet()) {
				if (this.isCancelled()) return;
				BagFile bagFile = bag.getBagFile(filepath);
				if (bagFile == null || ! bagFile.exists()) {
					deleteFilepaths.add(filepath);
				}
			}
			for(String filepath : deleteFilepaths) {
				manifest.remove(filepath);
			}
		}
	}
	
	public void handleManifest(final Bag bag, final Algorithm algorithm, String filepath, Collection<BagFile> bagFiles) {
		Manifest manifest = (Manifest)bag.getBagFile(filepath);
		if (manifest == null) {
			manifest = bag.getBagPartFactory().createManifest(filepath);
		}
		
		final int total = bagFiles.size();
    	final AtomicInteger count = new AtomicInteger();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		ArrayList<Future<Map<String,String>>> futures = new ArrayList<Future<Map<String,String>>>(this.numberOfThreads);
		//Since a Manifest is not synchronized, creating separate manifestEntry maps and merging
    	try {
	    	final Iterator<BagFile> bagFileIter = bagFiles.iterator();
		    for (int i = 0; i < this.numberOfThreads; i++) {
		    	log.debug(MessageFormat.format("Starting thread {0} of {1}.", i, this.numberOfThreads));
		    	Future<Map<String, String>> future = threadPool.submit(new Callable<Map<String,String>>() {
		            public Map<String,String> call() {
		                ThreadSafeIteratorWrapper<BagFile> safeIterator = new ThreadSafeIteratorWrapper<BagFile>(bagFileIter);
		                Map<String,String> manifestEntries = new LinkedHashMap<String, String>();
		                
		        		for(final BagFile bagFile : safeIterator) {
		        			if (isCancelled()) return null;
		        			progress("creating manifest entry", bagFile.getFilepath(), count.incrementAndGet(), total);
		        			if (ManifestHelper.isTagManifest(bagFile.getFilepath(), bag.getBagConstants())) {
		        				log.debug(MessageFormat.format("Skipping {0} since it is a tag manifest.", bagFile.getFilepath()));
		        			} else if (! bag.getChecksums(bagFile.getFilepath()).isEmpty()) {
		        				log.debug(MessageFormat.format("Checksum already exists for {0}.", bagFile.getFilepath()));
		        			} else {
		        				String checksum = MessageDigestHelper.generateFixity(bagFile.newInputStream(), algorithm);
		        				log.debug(MessageFormat.format("Generated fixity for {0}.", bagFile.getFilepath()));
		        				manifestEntries.put(bagFile.getFilepath(), checksum);
		        			}
		        		}
		        		VFSHelper.closeFileSystemManager();
		        		return manifestEntries;
		            }
	            });
		    	futures.add(future);
		    	
		    }
            for (Future<Map<String,String>> future : futures) {
                Map<String,String> futureResult = future.get();
                if (futureResult != null) {
                	manifest.putAll(futureResult);
                }
            }               

    	} catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }    	
    	finally {
        	log.debug("Shutting down thread pool.");
        	threadPool.shutdown();
        	log.debug("Thread pool shut down.");
        }
		bag.putBagFile(manifest);
	}

	public void regenerateManifest(final Bag bag, final Manifest manifest) {
		this.regenerateManifest(bag, manifest, false);
	}
	
	public void regenerateManifest(final Bag bag, final Manifest manifest, final boolean useOriginalPayloadManifests) {
		
		final int total = manifest.size();
    	final AtomicInteger count = new AtomicInteger();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		ArrayList<Future<Map<String,String>>> futures = new ArrayList<Future<Map<String,String>>>(this.numberOfThreads);
		//Since a Manifest is not synchronized, creating separate manifestEntry maps and merging
    	try {
	    	final Iterator<String> filepathIter = manifest.keySet().iterator();
		    for (int i = 0; i < this.numberOfThreads; i++) {
		    	log.debug(MessageFormat.format("Starting thread {0} of {1}.", i, this.numberOfThreads));
		    	Future<Map<String, String>> future = threadPool.submit(new Callable<Map<String,String>>() {
		            public Map<String,String> call() {
		                ThreadSafeIteratorWrapper<String> safeIterator = new ThreadSafeIteratorWrapper<String>(filepathIter);
		                Map<String,String> manifestEntries = new LinkedHashMap<String, String>();
		                
		        		for(final String filepath : safeIterator) {
		        			if (isCancelled()) return null;
		        			progress("creating manifest entry", filepath, count.incrementAndGet(), total);
		        			
	        				String checksum;
	        				if (useOriginalPayloadManifests && ManifestHelper.isPayloadManifest(filepath, bag.getBagConstants())) {
	        					checksum = MessageDigestHelper.generateFixity(((Manifest)bag.getBagFile(filepath)).originalInputStream(), manifest.getAlgorithm());
	        				} else {
	        					checksum = MessageDigestHelper.generateFixity(bag.getBagFile(filepath).newInputStream(), manifest.getAlgorithm());
	        				}
	        				log.debug(MessageFormat.format("Generated fixity for {0}.", filepath));
	        				manifestEntries.put(filepath, checksum);
		        			
		        		}
		        		VFSHelper.closeFileSystemManager();
		        		return manifestEntries;
		            }
	            });
		    	futures.add(future);
		    	
		    }
            for (Future<Map<String,String>> future : futures) {
                Map<String,String> futureResult = future.get();
                if (futureResult != null) {
                	manifest.putAll(futureResult);
                }
            }               

    	} catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }    	
    	finally {
        	log.debug("Shutting down thread pool.");
        	threadPool.shutdown();
        	log.debug("Thread pool shut down.");
        }
	}


}
