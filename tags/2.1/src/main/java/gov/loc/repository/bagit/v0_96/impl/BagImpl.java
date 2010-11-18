package gov.loc.repository.bagit.v0_96.impl;

import java.io.File;

import gov.loc.repository.bagit.impl.AbstractBagImpl;

public class BagImpl extends AbstractBagImpl {
		
	private static final BagConstants bagConstants = new BagConstantsImpl();
	
	private static final BagPartFactory bagPartFactory = new BagPartFactoryImpl();
	
	public BagImpl(File file) {
		super(file);
	}

	public BagImpl() {
		super();
		
	}
		
	public BagConstants getBagConstants() {
		return bagConstants;
	}
	
	public BagPartFactory getBagPartFactory() {
		return bagPartFactory;
	}
}