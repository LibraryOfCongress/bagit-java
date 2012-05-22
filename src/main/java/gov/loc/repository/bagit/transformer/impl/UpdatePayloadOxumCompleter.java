package gov.loc.repository.bagit.transformer.impl;

import java.util.Arrays;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.transformer.Completer;

public class UpdatePayloadOxumCompleter implements Completer {

	private CompleterHelper helper;
	private BagFactory bagFactory;
	
	//Not bothering with extending LongRunningOperation since this should be fast
	//Not bothering with configuration of threadcount
	
	public UpdatePayloadOxumCompleter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
		this.helper = new CompleterHelper();
	}
	

	
	@Override
	public Bag complete(Bag bag) {
		
		Bag newBag = this.bagFactory.createBag(bag);
		newBag.putBagFiles(bag.getPayload());
		newBag.putBagFiles(bag.getTags());
		
			BagInfoTxt bagInfo = newBag.getBagInfoTxt();
			if (bagInfo != null) {
				if (bagInfo.getPayloadOxum() != null) {
					bagInfo.generatePayloadOxum(newBag);
				}
			
			//Regenerate the tag manifests
			for(Manifest manifest : newBag.getTagManifests()) {
				this.helper.regenerateManifest(newBag, manifest, true, Arrays.asList(new String[] { newBag.getBagConstants().getBagInfoTxt()}), null);
			}
		}
		return newBag;
	}


}
