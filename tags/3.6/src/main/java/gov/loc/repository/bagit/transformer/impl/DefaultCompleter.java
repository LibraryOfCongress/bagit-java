package gov.loc.repository.bagit.transformer.impl;

import java.util.Calendar;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class DefaultCompleter extends LongRunningOperationBase implements Completer {
	
	private boolean generateTagManifest = true;
	private boolean updatePayloadOxum = true;
	private boolean updateBaggingDate = true;
	private boolean updateBagSize = true;
	private boolean generateBagInfoTxt = true;
	private boolean clearPayloadManifests = false;
	private boolean clearTagManifests = true;
	private boolean completePayloadManifests = true;
	private boolean completeTagManifests = true;
	private Algorithm tagManifestAlgorithm = Algorithm.MD5;
	private Algorithm payloadManifestAlgorithm = Algorithm.MD5;
	private Bag newBag;
	private BagFactory bagFactory;
	private CompleterHelper helper;
	private String nonDefaultManifestSeparator = null;
	
	public DefaultCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
		this.helper = new CompleterHelper();
		this.addChainedCancellable(this.helper);
		this.addChainedProgressListenable(this.helper);
	}
	
    public void setNumberOfThreads(int num) {
    	this.helper.setNumberOfThreads(num);
    }
	
    public void setCompleteTagManifests(boolean complete) {
    	this.completeTagManifests = complete;
    }

    public void setCompletePayloadManifests(boolean complete) {
    	this.completePayloadManifests = complete;
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
	public Bag complete(Bag bag) {		
		this.newBag = this.bagFactory.createBag(bag);
		this.newBag.putBagFiles(bag.getPayload());
		this.newBag.putBagFiles(bag.getTags());
		this.handleBagIt();
		this.handleBagInfo();
		if (this.completePayloadManifests) {
			this.handlePayloadManifests();
		}
		if (this.completeTagManifests) {
			this.handleTagManifests();
		}
		
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
			this.helper.clearManifests(this.newBag, this.newBag.getTagManifests());
		}
		this.helper.cleanManifests(this.newBag, this.newBag.getTagManifests());
		if (this.generateTagManifest) {
			this.helper.handleManifest(this.newBag, this.tagManifestAlgorithm, ManifestHelper.getTagManifestFilename(this.tagManifestAlgorithm, this.newBag.getBagConstants()), this.newBag.getTags(), this.nonDefaultManifestSeparator);
		}
	}
	
	protected void handlePayloadManifests() {
		if (this.clearPayloadManifests) {
			this.helper.clearManifests(this.newBag, this.newBag.getPayloadManifests());
		}
		this.helper.cleanManifests(this.newBag, this.newBag.getPayloadManifests());
		this.helper.handleManifest(this.newBag, this.payloadManifestAlgorithm, ManifestHelper.getPayloadManifestFilename(this.payloadManifestAlgorithm, this.newBag.getBagConstants()),this.newBag.getPayload(), this.nonDefaultManifestSeparator);		
	}
	
	public String getNonDefaultManifestSeparator() {
		return this.nonDefaultManifestSeparator;
	}
	
	public void setNonDefaultManifestSeparator(String manifestSeparator) {
		this.nonDefaultManifestSeparator = manifestSeparator;
	}

	
}
