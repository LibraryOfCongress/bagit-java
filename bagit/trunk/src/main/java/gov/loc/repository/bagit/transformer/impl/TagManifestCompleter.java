package gov.loc.repository.bagit.transformer.impl;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;

public class TagManifestCompleter implements Completer {

	private CompleterHelper helper;
	private BagFactory bagFactory;
	private Algorithm tagManifestAlgorithm = Algorithm.MD5;
	private String nonDefaultManifestSeparator = null;
	
	//Not bothering with extending LongRunningOperation since this should be fast
	//Not bothering with configuration of threadcount
	
	public TagManifestCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
		this.helper = new CompleterHelper();
	}
	
	public void setTagManifestAlgorithm(Algorithm tagManifestAlgorithm) {
		this.tagManifestAlgorithm = tagManifestAlgorithm;
	}
	
	@Override
	public Bag complete(Bag bag) {
		
		Bag newBag = this.bagFactory.createBag(bag);
		newBag.putBagFiles(bag.getPayload());
		newBag.putBagFiles(bag.getTags());

		//Delete anything that doesn't exist
		this.helper.cleanManifests(newBag, newBag.getTagManifests());
		
		//Regenerate the tag manifests
		for(Manifest manifest : newBag.getTagManifests()) {
			this.helper.regenerateManifest(newBag, manifest, true);
		}
		//See if anything is missing
		this.helper.handleManifest(newBag, this.tagManifestAlgorithm, ManifestHelper.getTagManifestFilename(this.tagManifestAlgorithm, newBag.getBagConstants()), newBag.getTags(), this.nonDefaultManifestSeparator);
		return newBag;
	}

	public String getNonDefaultManifestSeparator() {
		return this.nonDefaultManifestSeparator;
	}
	
	public void setNonDefaultManifestSeparator(String manifestSeparator) {
		this.nonDefaultManifestSeparator = manifestSeparator;
	}

}
