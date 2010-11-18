package gov.loc.repository.bagit.completion;

import java.util.Calendar;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.CompletionStrategy;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.impl.ManifestImpl;

public class DefaultCompletionStrategy implements CompletionStrategy {
	private boolean generateTagManifest = true;
	private boolean updatePayloadOxum = true;
	private boolean updateBaggingDate = true;
	private boolean updateBagSize = true;
	private boolean generateBagInfoTxt = true;
	private Algorithm tagManifestAlgorithm = Algorithm.MD5;
	private Algorithm payloadManifestAlgorithm = Algorithm.MD5;
	
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
	
	public void complete(Bag bag) {
		this.handleBagIt(bag);
		this.handleBagInfo(bag);
		this.handlePayloadManifests(bag);
		this.handleTagManifests(bag);
	}
	
	protected void handleBagIt(Bag bag) {
		bag.setBagItTxt(bag.getBagPartFactory().createBagItTxt());
	}
	
	protected void handleBagInfo(Bag bag) {
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		if (bagInfo == null) {
			if (this.generateBagInfoTxt) {
				bagInfo = bag.getBagPartFactory().createBagInfoTxt();
				bag.setBagInfoTxt(bagInfo);
			} else {
				return;
			}
		}
		if (this.updatePayloadOxum) {
			bagInfo.generatePayloadOxum(bag);
		}
		if (this.updateBaggingDate) {
			bagInfo.setBaggingDate(Calendar.getInstance().getTime());
		}
		if (this.updateBagSize) {
			bagInfo.generateBagSize(bag);
		}
		
	}
	
	protected void handleTagManifests(Bag bag) {
		//Remove existing tag manifests
		for(Manifest manifest : bag.getTagManifests()) {
			bag.removeTagFile(manifest.getFilepath());
		}
		if (this.generateTagManifest) {
			String filepath = ManifestHelper.getTagManifestFilename(this.tagManifestAlgorithm, bag.getBagConstants());
			Manifest manifest = new ManifestImpl(filepath, bag);
			manifest.generate(bag.getTagFiles());
			bag.putTagFile(manifest);
		}
	}
	
	protected void handlePayloadManifests(Bag bag) {
		//Remove existing payload manifests
		for(Manifest manifest : bag.getPayloadManifests()) {
			bag.removeTagFile(manifest.getFilepath());
		}
		String filepath = ManifestHelper.getPayloadManifestFilename(this.payloadManifestAlgorithm, bag.getBagConstants());
		Manifest manifest = new ManifestImpl(filepath, bag);
		manifest.generate(bag.getPayloadFiles());
		bag.putTagFile(manifest);
		
	}
}
