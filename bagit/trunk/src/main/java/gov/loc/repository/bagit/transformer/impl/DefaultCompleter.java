package gov.loc.repository.bagit.transformer.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

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

public class DefaultCompleter implements Completer, Cancellable, ProgressListenable {
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
	private ProgressListener progressIndicator;
	private BagFactory bagFactory;
	
	public DefaultCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
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
	public void setProgressIndicator(ProgressListener progressIndicator) {
		this.progressIndicator = progressIndicator;
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
			if (this.progressIndicator != null) progressIndicator.reportProgress("cleaning manifest", manifest.getFilepath(), manifestCount, manifestTotal);
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
	
	protected void handleManifest(Algorithm algorithm, String filepath, Collection<BagFile> bagFiles) {
		Manifest manifest = (Manifest)this.newBag.getBagFile(filepath);
		if (manifest == null) {
			manifest = this.newBag.getBagPartFactory().createManifest(filepath);
		}
		int count = 0;
		int total = bagFiles.size();
		for(BagFile bagFile : bagFiles) {
			if (this.cancelIndicator != null && this.cancelIndicator.performCancel()) return;
			count++;
			if (this.progressIndicator != null) progressIndicator.reportProgress("creating manifest entry", bagFile.getFilepath(), count, total);
			if (this.newBag.getFixities(bagFile.getFilepath()).isEmpty()) {
				String fixity = MessageDigestHelper.generateFixity(bagFile.newInputStream(), algorithm);
				manifest.put(bagFile.getFilepath(), fixity);
			}
		}
		this.newBag.putBagFile(manifest);
	}

}
