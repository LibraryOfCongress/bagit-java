package gov.loc.repository.bagit.verify;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.VerifyStrategy;
import gov.loc.repository.bagit.impl.BagInfoTxtImpl;
import gov.loc.repository.bagit.v0_96.impl.BagImpl;

import org.junit.Test;

public class RequiredBagInfoTxtFieldsStrategyTest {

	@Test
	public void testVerify() {
		VerifyStrategy strategy = new RequiredBagInfoTxtFieldsStrategy(new String[] {BagInfoTxtImpl.BAG_COUNT, BagInfoTxtImpl.BAG_GROUP_IDENTIFIER});
		
		Bag bag = new BagImpl();
		BagInfoTxt bagInfo = bag.getBagPartFactory().createBagInfoTxt();
		assertFalse(bag.additionalVerify(strategy).isSuccess());
		
		bag.setBagInfoTxt(bagInfo);
		assertFalse(bag.additionalVerify(strategy).isSuccess());
		bagInfo.setBagCount("1 of 2");
		assertFalse(bag.additionalVerify(strategy).isSuccess());
		bagInfo.setBagGroupIdentifier("foo");
		assertTrue(bag.additionalVerify(strategy).isSuccess());
		bagInfo.setBagSize("45 gb");
		assertTrue(bag.additionalVerify(strategy).isSuccess());
		
	}

}
