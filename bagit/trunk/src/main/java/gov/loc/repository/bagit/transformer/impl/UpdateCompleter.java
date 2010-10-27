package gov.loc.repository.bagit.transformer.impl;

import java.util.Calendar;
import java.util.List;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class UpdateCompleter extends LongRunningOperationBase implements Completer {
	
	private Algorithm tagManifestAlgorithm = null;
	private Algorithm payloadManifestAlgorithm = null;
	private Bag newBag;
	private BagFactory bagFactory;
	private CompleterHelper helper;
	private String nonDefaultManifestSeparator;
	private List<String> limitUpdateFilepaths = null;
	private List<String> limitDeleteFilepaths = null;
	private List<String> limitAddFilepaths = null;
	
	public UpdateCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
		this.helper = new CompleterHelper();
		this.addChainedCancellable(this.helper);
		this.addChainedProgressListenable(this.helper);
	}
	
	public void setLimitUpdatePayloadFilepaths(List<String> limitUpdateFiles) {
		this.limitUpdateFilepaths = limitUpdateFiles;
	}
	
	public void setLimitDeletePayloadFilepaths(List<String> limitDeleteFiles) {
		this.limitDeleteFilepaths = limitDeleteFiles;
	}
	
	public void setLimitAddPayloadFilepaths(List<String> limitAddFiles) {
		this.limitAddFilepaths = limitAddFiles;
	}
	
    public void setNumberOfThreads(int num) {
    	this.helper.setNumberOfThreads(num);
    }
	    
	public void setTagManifestAlgorithm(Algorithm tagManifestAlgorithm) {
		this.tagManifestAlgorithm = tagManifestAlgorithm;
	}

	public void setPayloadManifestAlgorithm(Algorithm payloadManifestAlgorithm) {
		this.payloadManifestAlgorithm = payloadManifestAlgorithm;
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
		
		if (this.isCancelled()) return null;
		
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
			//Do nothing
			return;
		}
		this.newBag.putBagFile(bagInfo);
		if (bagInfo.getPayloadOxum() != null) {
			bagInfo.generatePayloadOxum(this.newBag);
		}
		if (bagInfo.getBaggingDate() != null) {
			bagInfo.setBaggingDate(Calendar.getInstance().getTime());
		}
		if (bagInfo.getBagSize() != null) {
			bagInfo.generateBagSize(this.newBag);
		}
		
	}
	
	protected void handleTagManifests() {
		if (this.newBag.getTagManifests().size() > 0) {
			//Takes care of deleted files
			this.helper.cleanManifests(this.newBag, this.newBag.getTagManifests());
			//Takes care of changed files
			for(Manifest manifest : this.newBag.getTagManifests()) {
				this.helper.regenerateManifest(this.newBag, manifest);
			}
			//Look for any added 
			Algorithm algorithm = this.tagManifestAlgorithm;
			if (algorithm == null) {
				algorithm = this.newBag.getTagManifests().get(0).getAlgorithm();
			}
			this.helper.handleManifest(this.newBag, algorithm, ManifestHelper.getTagManifestFilename(algorithm, this.newBag.getBagConstants()), this.newBag.getTags(), this.nonDefaultManifestSeparator);
		}
	}
	
	protected void handlePayloadManifests() {
		//Takes care of deleted files
		this.helper.cleanManifests(this.newBag, this.newBag.getPayloadManifests(), this.limitDeleteFilepaths);
		//Takes care of changed files
		for(Manifest manifest : this.newBag.getPayloadManifests()) {
			this.helper.regenerateManifest(this.newBag, manifest, false, this.limitUpdateFilepaths);
		}
		//Looks for any added
		Algorithm algorithm = this.payloadManifestAlgorithm;
		if (algorithm == null) {
			if (this.newBag.getPayloadManifests().size() > 0) {
				algorithm = this.newBag.getPayloadManifests().get(0).getAlgorithm();
			} else {
				algorithm = Algorithm.MD5;
			}
		}
		this.helper.handleManifest(this.newBag, algorithm, ManifestHelper.getPayloadManifestFilename(algorithm, this.newBag.getBagConstants()),this.newBag.getPayload(), this.nonDefaultManifestSeparator, this.limitAddFilepaths);		
	}
	
	public String getNonDefaultManifestSeparator() {
		return this.nonDefaultManifestSeparator;
	}
	
	public void setNonDefaultManifestSeparator(String manifestSeparator) {
		this.nonDefaultManifestSeparator = manifestSeparator;
	}

}
