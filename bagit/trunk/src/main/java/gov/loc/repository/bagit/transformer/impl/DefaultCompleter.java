package gov.loc.repository.bagit.transformer.impl;

import java.util.Calendar;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.impl.ManifestImpl;
import gov.loc.repository.bagit.transformer.Completer;

public class DefaultCompleter implements Completer {
	private boolean generateTagManifest = true;
	private boolean updatePayloadOxum = true;
	private boolean updateBaggingDate = true;
	private boolean updateBagSize = true;
	private boolean generateBagInfoTxt = true;
	private Algorithm tagManifestAlgorithm = Algorithm.MD5;
	private Algorithm payloadManifestAlgorithm = Algorithm.MD5;
	private Bag newBag;
	
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
	
	@Override
	public Bag complete(Bag bag) {
		this.newBag = BagFactory.createBag(bag);
		this.newBag.putBagFiles(bag.getPayload());
		this.newBag.putBagFiles(bag.getTags());
		this.handleBagIt();
		this.handleBagInfo();
		this.handlePayloadManifests();
		this.handleTagManifests();
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
		if (this.generateTagManifest) {
			String filepath = ManifestHelper.getTagManifestFilename(this.tagManifestAlgorithm, this.newBag.getBagConstants());
			Manifest manifest = new ManifestImpl(filepath, this.newBag);
			manifest.generate(this.newBag.getTags());
			this.newBag.putBagFile(manifest);
		}
	}
	
	protected void handlePayloadManifests() {
		String filepath = ManifestHelper.getPayloadManifestFilename(this.payloadManifestAlgorithm, this.newBag.getBagConstants());
		Manifest manifest = new ManifestImpl(filepath, this.newBag);
		manifest.generate(this.newBag.getPayload());
		this.newBag.putBagFile(manifest);
		
	}
}
