package gov.loc.repository.bagit.verify.impl;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.impl.BagInfoTxtImpl;
import gov.loc.repository.bagit.v0_96.impl.BagImpl;
import gov.loc.repository.bagit.verify.AdditionalVerifier;
import gov.loc.repository.bagit.verify.impl.RequiredBagInfoTxtFieldsStrategy;

import org.junit.Test;

public class RequiredBagInfoTxtFieldsStrategyTest {

	@Test
	public void testVerify() {
		AdditionalVerifier strategy = new RequiredBagInfoTxtFieldsStrategy(new String[] {BagInfoTxtImpl.FIELD_BAG_COUNT, BagInfoTxtImpl.FIELD_BAG_GROUP_IDENTIFIER});
		
		Bag bag = new BagImpl();
		BagInfoTxt bagInfo = bag.getBagPartFactory().createBagInfoTxt();
		assertFalse(bag.checkAdditionalVerify(strategy).isSuccess());
		
		bag.putBagFile(bagInfo);
		assertFalse(bag.checkAdditionalVerify(strategy).isSuccess());
		bagInfo.setBagCount("1 of 2");
		assertFalse(bag.checkAdditionalVerify(strategy).isSuccess());
		bagInfo.setBagGroupIdentifier("foo");
		assertTrue(bag.checkAdditionalVerify(strategy).isSuccess());
		bagInfo.setBagSize("45 gb");
		assertTrue(bag.checkAdditionalVerify(strategy).isSuccess());
		
	}

}
