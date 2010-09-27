package gov.loc.repository.bagit.v0_93.impl;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.impl.AbstractBag;

public class BagImpl extends AbstractBag {

	public BagImpl(BagFactory bagFactory) {
		super(new BagPartFactoryImpl(bagFactory, new BagConstantsImpl()), new BagConstantsImpl(), bagFactory);
	}

}
