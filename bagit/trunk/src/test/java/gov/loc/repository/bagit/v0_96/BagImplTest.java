package gov.loc.repository.bagit.v0_96;

import static org.junit.Assert.*;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.utilities.ResourceHelper;


public class BagImplTest {

	private Bag getBag(String filepath) throws Exception {
		return BagFactory.createBag(ResourceHelper.getFile(filepath), Version.V0_96);  
	}
		
	@Test
	public void testCompleteBags() throws Exception {
		assertTrue(this.getBag("bags/v0_96/bag_with_one_manifest").isComplete().isSuccess());
	}

	@Test
	public void testValidBags() throws Exception {
		assertTrue(this.getBag("bags/v0_96/bag_with_one_manifest").isValid().isSuccess());
	}
	
	@Test(expected=RuntimeException.class)
	public void testBackslashFilename() throws Exception {
		this.getBag("bags/v0_96/bag_with_backslash_filename.tar").isComplete();
	}
	
	@Test
	public void testBag() throws Exception {
		Bag bag = this.getBag("bags/v0_96/bag_with_one_manifest");
		assertEquals("0.96", bag.getBagItTxt().getVersion());
		assertNotNull(bag.getBagInfoTxt());
	}
	
}