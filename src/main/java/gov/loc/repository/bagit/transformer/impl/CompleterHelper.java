package gov.loc.repository.bagit.transformer.impl;

import java.io.InputStream;
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

	private boolean filepathListContains(List<String> filepaths, String filepath) {
		boolean res = false;
		if (filepaths != null && filepaths.contains(filepath)) res = true;
		log.trace(MessageFormat.format("Checking if filepath list contains {0}: {1}", filepath, res));
		return res;
	}
	
	private boolean dirListContains(List<String> dirs, String filepath) {
		boolean res = false;
		if (dirs != null) {
			for(String dir : dirs) {
				if (! dir.endsWith("/")) dir += "/";
				if (filepath.startsWith(dir)) {
					res = true;
					break;
				}
			}
		}
		log.trace(MessageFormat.format("Checking if directory list contains {0}: {1}", filepath, res));
		return res;
	}
	
	private boolean isLimited(String filepath, List<String> filepaths, List<String> dirs) {
		boolean res = false;
		if (filepaths == null && dirs == null) {
			res = true;
		} else if (dirListContains(dirs, filepath) || filepathListContains(filepaths, filepath)) {
			res = true;
		}
		log.trace(MessageFormat.format("Checking if {0} is limited: {1}", filepath, res));
		return res;
	}
	
	public void cleanManifests(Bag bag, Collection<Manifest> manifests) {
		this.cleanManifests(bag, manifests, null, null);
	}
	
	public void cleanManifests(Bag bag, Collection<Manifest> manifests, List<String> limitDeleteFilepaths, List<String> limitDeleteDirectories) {
		int manifestTotal = manifests.size();
		int manifestCount = 0;
		for(Manifest manifest : manifests) {			
			manifestCount++;
			
			this.progress("cleaning manifest", manifest.getFilepath(), manifestCount, manifestTotal);
			
			List<String> deleteFilepaths = new ArrayList<String>();
			for(String filepath : manifest.keySet()) {
				if (this.isCancelled()) return;
				BagFile bagFile = bag.getBagFile(filepath);
				if ((bagFile == null || ! bagFile.exists()) && isLimited(filepath, limitDeleteFilepaths, limitDeleteDirectories)) {
					deleteFilepaths.add(filepath);
				}
			}
			for(String filepath : deleteFilepaths) {
				manifest.remove(filepath);
			}
		}
	}

	public void handleManifest(final Bag bag, final Algorithm algorithm, String filepath, Collection<BagFile> bagFiles, String nonDefaultManifestSeparator) {
		this.handleManifest(bag, algorithm, filepath, bagFiles, nonDefaultManifestSeparator, null, null);
	}
	
	public void handleManifest(final Bag bag, final Algorithm algorithm, String filepath, Collection<BagFile> bagFiles, String nonDefaultManifestSeparator, final List<String> limitAddFilepaths, final List<String> limitAddDirectories) {
		Manifest manifest = (Manifest)bag.getBagFile(filepath);
		if (manifest == null) {
			manifest = bag.getBagPartFactory().createManifest(filepath);
		}
	
		manifest.setNonDefaultManifestSeparator(nonDefaultManifestSeparator);
		
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
		        			if (isLimited(bagFile.getFilepath(), limitAddFilepaths, limitAddDirectories)) {
			        			if (ManifestHelper.isTagManifest(bagFile.getFilepath(), bag.getBagConstants())) {
			        				log.debug(MessageFormat.format("Skipping {0} since it is a tag manifest.", bagFile.getFilepath()));
			        			} else if (! bag.getChecksums(bagFile.getFilepath()).isEmpty()) {
			        				log.debug(MessageFormat.format("Checksum already exists for {0}.", bagFile.getFilepath()));
			        			} else {
			        				String checksum = MessageDigestHelper.generateFixity(bagFile.newInputStream(), algorithm);
			        				log.debug(MessageFormat.format("Generated fixity for {0}.", bagFile.getFilepath()));
			        				manifestEntries.put(bagFile.getFilepath(), checksum);
			        			}
		        			} else {
		        				log.trace(MessageFormat.format("{0} not in limit add filepaths or limit add directories", bagFile.getFilepath()));
		        			}
		        		}
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
    	if (! manifest.isEmpty()) bag.putBagFile(manifest);
	}

	public void regenerateManifest(final Bag bag, final Manifest manifest) {
		this.regenerateManifest(bag, manifest, false);
	}

	public void regenerateManifest(final Bag bag, final Manifest manifest, final boolean useOriginalPayloadManifests) {
		this.regenerateManifest(bag, manifest, useOriginalPayloadManifests, null, null);
	}

	public void regenerateManifest(final Bag bag, final Manifest manifest, final boolean useOriginalPayloadManifests, final List<String> limitUpdateFilepaths, final List<String> limitUpdateDirectories) {
		log.debug("Regenerating " + manifest.getFilepath());
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
		        			log.trace(MessageFormat.format("Creating manifest entry for {0}", filepath));
	        				String checksum = manifest.get(filepath);
	        				if (isLimited(filepath, limitUpdateFilepaths, limitUpdateDirectories) && bag.getBagFile(filepath) != null) {
		        				log.debug(MessageFormat.format("Generating fixity for {0}.", filepath));
		        				InputStream in = null;		        				
	        					if (useOriginalPayloadManifests && ManifestHelper.isPayloadManifest(filepath, bag.getBagConstants())) {
	        						//originalInputStream may be null
	        						in = ((Manifest)bag.getBagFile(filepath)).originalInputStream();
	        					}
	        					if (in == null) in = bag.getBagFile(filepath).newInputStream();
	        					checksum = MessageDigestHelper.generateFixity(in, manifest.getAlgorithm());
	        				}
	        				manifestEntries.put(filepath, checksum);
		        			
		        		}
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
