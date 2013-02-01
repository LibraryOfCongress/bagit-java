package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFile;

public class FetchProgressTxtImpl extends AbstractFetchTxtImpl {

	private static final long serialVersionUID = 1L;

	public FetchProgressTxtImpl(BagConstants bagConstants, BagPartFactory bagPartFactory) {
		super(bagConstants, bagPartFactory);
	}
	
	public FetchProgressTxtImpl(BagConstants bagConstants, BagPartFactory bagPartFactory, BagFile sourceBagFile) {
		super(bagConstants, bagPartFactory, sourceBagFile);
	}

	public String getName(){
		return this.bagConstants.getFetchProgressTxt();
	}
}
