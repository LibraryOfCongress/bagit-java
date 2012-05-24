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
	private List<String> limitUpdatePayloadFilepaths = null;
	private List<String> limitDeletePayloadFilepaths = null;
	private List<String> limitAddPayloadFilepaths = null;
	private List<String> limitUpdatePayloadDirectories = null;
	private List<String> limitDeletePayloadDirectories = null;
	private List<String> limitAddPayloadDirectories = null;
	private List<String> limitUpdateTagFilepaths = null;
	private List<String> limitDeleteTagFilepaths = null;
	private List<String> limitAddTagFilepaths = null;
	private List<String> limitUpdateTagDirectories = null;
	private List<String> limitDeleteTagDirectories = null;
	private List<String> limitAddTagDirectories = null;
	
	public UpdateCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
		this.helper = new CompleterHelper();
		this.addChainedCancellable(this.helper);
		this.addChainedProgressListenable(this.helper);
	}
	
	/*
	 * Limit updates to the provided payload filepaths, i.e., only the manifest entries of the
	 * provided files will be updated.
	 */
	public void setLimitUpdatePayloadFilepaths(List<String> limitUpdateFiles) {
		this.limitUpdatePayloadFilepaths = limitUpdateFiles;
	}
	
	/*
	 * Limit deletes to the provided payload filepaths, i.e., only the manifest entries of the
	 * provided files will be removed. 
	 */
	public void setLimitDeletePayloadFilepaths(List<String> limitDeleteFiles) {
		this.limitDeletePayloadFilepaths = limitDeleteFiles;
	}
	
	/*
	 * Limit additions to the provided payload filepaths, i.e., only manifest entries for the
	 * provided files will be added.
	 */
	public void setLimitAddPayloadFilepaths(List<String> limitAddFiles) {
		this.limitAddPayloadFilepaths = limitAddFiles;
	}

	/*
	 * Limit updates to the payload files in and below the provided directories, i.e.,
	 * only the manifest entries of files in or below the provided directories will be updated.
	 */
	public void setLimitUpdatePayloadDirectories(List<String> limitUpdateDirectories) {
		this.limitUpdatePayloadDirectories = limitUpdateDirectories;
	}
	
	/*
	 * Limit deletes to the payload files in and below the provided directories, i.e.,
	 * only the manifest entries of files in or below the provided directories will be removed.
	 */
	public void setLimitDeletePayloadDirectories(List<String> limitDeleteDirectories) {
		this.limitDeletePayloadDirectories = limitDeleteDirectories;
	}
	
	/*
	 * Limit additions to the payload files in and below the provided directories, i.e.,
	 * only manifest entries for files in or below the provided directories will be added.
	 */
	public void setLimitAddPayloadDirectories(List<String> limitAddDirectories) {
		this.limitAddPayloadDirectories = limitAddDirectories;
	}

	/*
	 * Limit updates to the provided tag filepaths, i.e., only the manifest entries of the
	 * provided files will be updated.
	 */
	public void setLimitUpdateTagFilepaths(List<String> limitUpdateFiles) {
		this.limitUpdateTagFilepaths = limitUpdateFiles;
	}
	
	/*
	 * Limit deletes to the provided tag filepaths, i.e., only the manifest entries of the
	 * provided files will be removed. 
	 */
	public void setLimitDeleteTagFilepaths(List<String> limitDeleteFiles) {
		this.limitDeleteTagFilepaths = limitDeleteFiles;
	}
	
	/*
	 * Limit additions to the provided tag filepaths, i.e., only manifest entries for the
	 * provided files will be added.
	 */
	public void setLimitAddTagFilepaths(List<String> limitAddFiles) {
		this.limitAddTagFilepaths = limitAddFiles;
	}

	/*
	 * Limit updates to the tag files in and below the provided directories, i.e.,
	 * only the manifest entries of files in or below the provided directories will be updated.
	 */
	public void setLimitUpdateTagDirectories(List<String> limitUpdateDirectories) {
		this.limitUpdateTagDirectories = limitUpdateDirectories;
	}
	
	/*
	 * Limit deletes to the tag files in and below the provided directories, i.e.,
	 * only the manifest entries of files in or below the provided directories will be removed.
	 */
	public void setLimitDeleteTagDirectories(List<String> limitDeleteDirectories) {
		this.limitDeleteTagDirectories = limitDeleteDirectories;
	}
	
	/*
	 * Limit additions to the tag files in and below the provided directories, i.e.,
	 * only manifest entries for files in or below the provided directories will be added.
	 */
	public void setLimitAddTagDirectories(List<String> limitAddDirectories) {
		this.limitAddTagDirectories = limitAddDirectories;
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
			if (this.limitAddTagFilepaths != null) this.limitAddTagFilepaths.add(this.newBag.getBagConstants().getBagItTxt());
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
		if (this.limitUpdateTagFilepaths != null) this.limitUpdateTagFilepaths.add(this.newBag.getBagConstants().getBagInfoTxt());

		
	}
	
	protected void handleTagManifests() {
		if (this.newBag.getTagManifests().size() > 0) {
			//Takes care of deleted files
			this.helper.cleanManifests(this.newBag, this.newBag.getTagManifests(), this.limitDeleteTagFilepaths, this.limitDeleteTagDirectories);
			//Takes care of changed files
			for(Manifest manifest : this.newBag.getTagManifests()) {
				this.helper.regenerateManifest(this.newBag, manifest, false, this.limitUpdateTagFilepaths, this.limitUpdateTagDirectories);
			}
			//Look for any added 
			Algorithm algorithm = this.tagManifestAlgorithm;
			if (algorithm == null) {
				algorithm = this.newBag.getTagManifests().get(0).getAlgorithm();
			}
			this.helper.handleManifest(this.newBag, algorithm, ManifestHelper.getTagManifestFilename(algorithm, this.newBag.getBagConstants()), this.newBag.getTags(), this.nonDefaultManifestSeparator, this.limitAddTagFilepaths, this.limitAddTagDirectories);
		}
	}
	
	protected void handlePayloadManifests() {
		//Takes care of deleted files
		this.helper.cleanManifests(this.newBag, this.newBag.getPayloadManifests(), this.limitDeletePayloadFilepaths, this.limitDeletePayloadDirectories);
		//Takes care of changed files
		for(Manifest manifest : this.newBag.getPayloadManifests()) {
			this.helper.regenerateManifest(this.newBag, manifest, false, this.limitUpdatePayloadFilepaths, this.limitUpdatePayloadDirectories);
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
		this.helper.handleManifest(this.newBag, algorithm, ManifestHelper.getPayloadManifestFilename(algorithm, this.newBag.getBagConstants()),this.newBag.getPayload(), this.nonDefaultManifestSeparator, this.limitAddPayloadFilepaths, this.limitAddPayloadDirectories);
		
		if (this.limitUpdateTagFilepaths != null) {
			for(Manifest manifest : this.newBag.getPayloadManifests()) {
				this.limitUpdateTagFilepaths.add(manifest.getFilepath());
			}
		}
			
			

	}
	
	public String getNonDefaultManifestSeparator() {
		return this.nonDefaultManifestSeparator;
	}
	
	public void setNonDefaultManifestSeparator(String manifestSeparator) {
		this.nonDefaultManifestSeparator = manifestSeparator;
	}

}
