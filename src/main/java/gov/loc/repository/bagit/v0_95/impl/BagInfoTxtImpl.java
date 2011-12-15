package gov.loc.repository.bagit.v0_95.impl;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;

public class BagInfoTxtImpl extends gov.loc.repository.bagit.impl.BagInfoTxtImpl implements BagInfoTxt {

	public static final String FIELD_PACKING_DATE = "Packing-Date";
	public static final String FIELD_PACKAGE_SIZE = "Package-Size";
	
	public BagInfoTxtImpl(BagFile bagFile, BagConstants bagConstants) {
		super(bagFile, bagConstants);
	}
	
	public BagInfoTxtImpl(BagConstants bagConstants) {
		super(bagConstants);
			
	}
	
	@Override
	public String getBagSize() {
		return this.getCaseInsensitive(FIELD_PACKAGE_SIZE);
	}

	@Override
	public void setBagSize(String bagSize) {
		this.put(FIELD_PACKAGE_SIZE, bagSize);
	}
	
	@Override
	public String getBaggingDate() {
		return this.getCaseInsensitive(FIELD_PACKING_DATE);
	}
	
	@Override
	public void setBaggingDate(String baggingDate) {
		this.put(FIELD_PACKING_DATE, baggingDate);
	}
	
}
