package gov.loc.repository.bagit.transformer.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.ThreadSafeIteratorWrapper;

public class DefaultCompleter implements Completer, Cancellable, ProgressListenable {
	
    private static final Log log = LogFactory.getLog(DefaultCompleter.class);
	
	private boolean generateTagManifest = true;
	private boolean updatePayloadOxum = true;
	private boolean updateBaggingDate = true;
	private boolean updateBagSize = true;
	private boolean generateBagInfoTxt = true;
	private boolean clearPayloadManifests = false;
	private boolean clearTagManifests = true;
	private Algorithm tagManifestAlgorithm = Algorithm.MD5;
	private Algorithm payloadManifestAlgorithm = Algorithm.MD5;
	private Bag newBag;
	private CancelIndicator cancelIndicator;
	private ProgressListener progressListener;
	private BagFactory bagFactory;
	private int numberOfThreads = 1;
	
	public DefaultCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
		this.numberOfThreads = Runtime.getRuntime().availableProcessors();
	}
	
    public void setNumberOfThreads(int num) {
        if (num < 1)
            throw new IllegalArgumentException("Number of threads must be at least 1.");
        
        this.numberOfThreads = num;
    }
	
	public void setGenerateTagManifest(boolean generateTagManifest) {
		this.generateTagManifest = generateTagManifest;
	}

	public void setTagManifestAlgorithm(Algorithm tagManifestAlgorithm) {
		this.tagManifestAlgorithm = tagManifestAlgorithm;
	}

	public void setPayloadManifestAlgorithm(Algorithm payloadManifestAlgorithm) {
		this.payloadManifestAlgorithm = payloadManifestAlgorithm;
	}

	public void setUpdatePayloadOxum(boolean updatePayloadOxum) {
		this.updatePayloadOxum = updatePayloadOxum;
	}

	public void setUpdateBaggingDate(boolean updateBaggingDate) {
		this.updateBaggingDate = updateBaggingDate;
	}
	
	public void setUpdateBagSize(boolean updateBagSize) {
		this.updateBagSize = updateBagSize;
	}
	
	public void setGenerateBagInfoTxt(boolean generateBagInfoTxt) {
		this.generateBagInfoTxt = generateBagInfoTxt;
	}
	
	public void setClearExistingTagManifests(boolean clearTagManifests) {
		this.clearTagManifests = clearTagManifests;
	}
	
	public void setClearExistingPayloadManifests(boolean clearPayloadManifests) {
		this.clearPayloadManifests = clearPayloadManifests;
	}
		
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;		
	}
	
	@Override
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	@Override
	public Bag complete(Bag bag) {		
		this.newBag = this.bagFactory.createBag(bag);
		this.newBag.putBagFiles(bag.getPayload());
		this.newBag.putBagFiles(bag.getTags());
		this.handleBagIt();
		this.handleBagInfo();
		this.handlePayloadManifests();
		this.handleTagManifests();
		if (this.cancelIndicator != null && this.cancelIndicator.performCancel()) {
			return null;
		}
		return this.newBag;
	}
	
	protected void handleBagIt() {
		if (this.newBag.getBagItTxt() == null) {
			this.newBag.putBagFile(this.newBag.getBagPartFactory().createBagItTxt());
		}
	}
	
	protected void handleBagInfo() {
		BagInfoTxt bagInfo = this.newBag.getBagInfoTxt();
		if (bagInfo == null) {
			if (this.generateBagInfoTxt) {				
				bagInfo = this.newBag.getBagPartFactory().createBagInfoTxt();
			} else {
				return;
			}
		}
		this.newBag.putBagFile(bagInfo);
		
		if (this.updatePayloadOxum) {
			bagInfo.generatePayloadOxum(this.newBag);
		}
		if (this.updateBaggingDate) {
			bagInfo.setBaggingDate(Calendar.getInstance().getTime());
		}
		if (this.updateBagSize) {
			bagInfo.generateBagSize(this.newBag);
		}
		
	}
	
	protected void handleTagManifests() {
		if (this.clearTagManifests) {
			this.clearManifests(this.newBag.getTagManifests());
		}
		this.cleanManifests(this.newBag.getTagManifests());
		if (this.generateTagManifest) {
			this.handleManifest(this.tagManifestAlgorithm, ManifestHelper.getTagManifestFilename(this.tagManifestAlgorithm, this.newBag.getBagConstants()), this.newBag.getTags());
		}
	}
	
	protected void handlePayloadManifests() {
		if (this.clearPayloadManifests) {
			this.clearManifests(this.newBag.getPayloadManifests());
		}
		this.cleanManifests(this.newBag.getPayloadManifests());
		this.handleManifest(this.payloadManifestAlgorithm, ManifestHelper.getPayloadManifestFilename(this.payloadManifestAlgorithm, this.newBag.getBagConstants()),this.newBag.getPayload());		
	}

	protected void clearManifests(Collection<Manifest> manifests) {
		for(Manifest manifest : manifests) {
			this.newBag.removeBagFile(manifest.getFilepath());
		}
	}
	
	protected void cleanManifests(Collection<Manifest> manifests) {
		int manifestTotal = manifests.size();
		int manifestCount = 0;
		for(Manifest manifest : manifests) {			
			manifestCount++;
			if (this.progressListener != null) progressListener.reportProgress("cleaning manifest", manifest.getFilepath(), manifestCount, manifestTotal);
			List<String> deleteFilepaths = new ArrayList<String>();
			for(String filepath : manifest.keySet()) {
				if (this.cancelIndicator != null && this.cancelIndicator.performCancel()) return;
				BagFile bagFile = this.newBag.getBagFile(filepath);
				if (bagFile == null || ! bagFile.exists()) {
					deleteFilepaths.add(filepath);
				}
			}
			for(String filepath : deleteFilepaths) {
				manifest.remove(filepath);
			}
		}
	}
	
	protected void handleManifest(final Algorithm algorithm, String filepath, Collection<BagFile> bagFiles) {
		Manifest manifest = (Manifest)this.newBag.getBagFile(filepath);
		if (manifest == null) {
			manifest = this.newBag.getBagPartFactory().createManifest(filepath);
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
		        			if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
		        			if (progressListener != null) progressListener.reportProgress("creating manifest entry", bagFile.getFilepath(), count.incrementAndGet(), total);
		        			if (newBag.getChecksums(bagFile.getFilepath()).isEmpty()) {
		        				String checksum = MessageDigestHelper.generateFixity(bagFile.newInputStream(), algorithm);
		        				log.debug(MessageFormat.format("Generated fixity for {0}.", bagFile.getFilepath()));
		        				manifestEntries.put(bagFile.getFilepath(), checksum);
		        			} else {
		        				log.debug(MessageFormat.format("Checksum already exists for {0}.", bagFile.getFilepath()));
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
		this.newBag.putBagFile(manifest);
	}

	@Override
	public CancelIndicator getCancelIndicator() {
		return this.cancelIndicator;
	}
}
