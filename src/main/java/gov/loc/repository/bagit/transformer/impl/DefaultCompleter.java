package gov.loc.repository.bagit.transformer.impl;

import java.text.MessageFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class DefaultCompleter extends LongRunningOperationBase implements Completer {
	
	private static final Log log = LogFactory.getLog(DefaultCompleter.class);
	
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
		log.info(MessageFormat.format("Completing bag at {0}", bag.getFile()));
		
		log.debug("Creating new bag and adding bag files from existing bag");
		this.newBag = this.bagFactory.createBag(bag);
		this.newBag.putBagFiles(bag.getPayload());
		this.newBag.putBagFiles(bag.getTags());
		
		log.debug("Handling bagit.txt");
		this.handleBagIt();
		
		log.debug("Handling bag-info.txt");
		this.handleBagInfo();
		
		if (this.completePayloadManifests) {
			log.debug("Completing payload manifests");
			this.handlePayloadManifests();
		} else {
			log.trace("Not completing payload manifests");
		}
		
		if (this.completeTagManifests) {
			log.debug("Completing tag manifests");
			this.handleTagManifests();
		} else {
			log.trace("Not completing tag manifests");
		}
		
		if (this.isCancelled()) return null;
		
		log.trace("Done completing");
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
			log.debug("Generating payload-oxum");
			bagInfo.generatePayloadOxum(this.newBag);
		} else {
			log.trace("Not generating payload-oxum");
		}
		
		if (this.updateBaggingDate) {
			log.debug("Setting bagging date");
			bagInfo.setBaggingDate(Calendar.getInstance().getTime());
		} else {
			log.trace("Not setting bagging date");
		}
		
		if (this.updateBagSize) {
			log.debug("Generating bag size");
			bagInfo.generateBagSize(this.newBag);
		} else {
			log.debug("Not generating bag size");
		}
		
	}
	
	protected void handleTagManifests() {
		if (this.clearTagManifests) {
			log.debug("Clearing tag manifests");
			this.helper.clearManifests(this.newBag, this.newBag.getTagManifests());
		} else {
			log.trace("Not clearing tag manifests");
		}
		
		log.debug("Cleaning tag manifests");
		this.helper.cleanManifests(this.newBag, this.newBag.getTagManifests());
		
		if (this.generateTagManifest) {
			log.debug("Generating tag manifests");
			this.helper.handleManifest(this.newBag, this.tagManifestAlgorithm, ManifestHelper.getTagManifestFilename(this.tagManifestAlgorithm, this.newBag.getBagConstants()), this.newBag.getTags(), this.nonDefaultManifestSeparator);
		} else {
			log.trace("Generating tag manifests");
		}
	}
	
	protected void handlePayloadManifests() {		
		if (this.clearPayloadManifests) {
			log.debug("Clearing payload manifests");
			this.helper.clearManifests(this.newBag, this.newBag.getPayloadManifests());
		} else {
			log.trace("Not clearing payload manifests");
		}
		
		log.debug("Cleaning payload manifests");
		this.helper.cleanManifests(this.newBag, this.newBag.getPayloadManifests());
		
		log.debug("Generating payload manifests");
		this.helper.handleManifest(this.newBag, this.payloadManifestAlgorithm, ManifestHelper.getPayloadManifestFilename(this.payloadManifestAlgorithm, this.newBag.getBagConstants()),this.newBag.getPayload(), this.nonDefaultManifestSeparator);		
	}
	
	public String getNonDefaultManifestSeparator() {
		return this.nonDefaultManifestSeparator;
	}
	
	public void setNonDefaultManifestSeparator(String manifestSeparator) {
		this.nonDefaultManifestSeparator = manifestSeparator;
	}
	
}
