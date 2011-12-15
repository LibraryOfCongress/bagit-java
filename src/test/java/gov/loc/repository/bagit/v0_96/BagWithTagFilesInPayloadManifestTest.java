package gov.loc.repository.bagit.v0_96;

import static org.junit.Assert.*;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractBagImplTest;

public class BagWithTagFilesInPayloadManifestTest extends AbstractBagImplTest {

	@Override
	public Version getVersion() {
		return Version.V0_96;
	}
	
	@Override
	protected String getBagName() {
		return "bag-with-tagfiles-in-payload-manifest";
	};
	
	@Override
	protected void assertBagComplete(Bag bag) throws Exception
	{
		assertFalse("Bag verified as complete when it shouldn't have.", bag.verifyValid().isSuccess());
	}

	@Override
	protected void assertBagValid(Bag bag) throws Exception
	{
		assertFalse("Bag verified as valid when it shouldn't have.", bag.verifyValid().isSuccess());
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
