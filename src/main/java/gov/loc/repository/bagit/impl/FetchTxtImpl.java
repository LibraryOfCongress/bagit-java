package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFile;

public class FetchTxtImpl extends AbstractFetchTxtImpl {

	private static final long serialVersionUID = 1L;
	
	public FetchTxtImpl(BagConstants bagConstants, BagPartFactory bagPartFactory) {
		super(bagConstants, bagPartFactory);
	}
	
	public FetchTxtImpl(BagConstants bagConstants, BagPartFactory bagPartFactory, BagFile sourceBagFile) {
		super(bagConstants, bagPartFactory, sourceBagFile);
	}
	
	public String getName(){
		return this.bagConstants.getFetchTxt();
	}
}
