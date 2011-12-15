package gov.loc.repository.bagit.v0_94;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractBagImplTest;

public class BagImplTest extends AbstractBagImplTest {

	@Override
	public Version getVersion() {
		return Version.V0_94;
	}

	@Override
	public void performAddlTestCreateBag(Bag bag) {
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		assertEquals(bagInfo.getBagSize(), bagInfo.get(gov.loc.repository.bagit.v0_94.impl.BagInfoTxtImpl.FIELD_PACKAGE_SIZE));
		assertEquals(bagInfo.getBaggingDate(), bagInfo.get(gov.loc.repository.bagit.v0_94.impl.BagInfoTxtImpl.FIELD_PACKING_DATE));

	}
	
	@Override
	public void performTestBagWithTagDirectory(Bag bag) {
		performTestBagWithTagDirectoryPrev97(bag);		
	}
	
	@Override
	public void performTestBagWithIgnoredTagDirectory(Bag bag) {
		performTestBagWithIgnoredTagDirectoryPrev97(bag);		
	}

}