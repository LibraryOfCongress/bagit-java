package gov.loc.repository.bagit.v0_97;

import static org.junit.Assert.*;

import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.impl.AbstractBagImplTest;
import gov.loc.repository.bagit.utilities.ResourceHelper;

public class BagImplTest extends AbstractBagImplTest {

	@Override
	public Version getVersion() {
		return Version.V0_97;
	}
	
	@Override
	public void performTestBagWithTagDirectory(Bag bag) {
		performTestBagWithTagDirectoryPostv97(bag);		
	}
	
	@Override
	public void performTestBagWithIgnoredTagDirectory(Bag bag) {
		performTestBagWithIgnoredTagDirectoryPost97(bag);		
	}
	
	protected void assertBagTagFiles(Bag bag) throws Exception
	{
		assertEquals(5, bag.getTags().size());
		assertNotNull(bag.getBagFile("bagit.txt"));
		assertNotNull(bag.getBagFile("addl_tags/tag1.txt"));
		assertNull(bag.getBagFile("xbagit.txt"));
	}

	@Override
	public void addlTestCreateBag(Bag bag) throws Exception {
		bag.addFileAsTag(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/addl_tags", this.getVersion().toString().toLowerCase())));
	}
	
	@Override
	public void performAddlTestCreateBag(Bag bag) {
		BagFile tag1BagFile = bag.getBagFile("addl_tags/tag1.txt");
		assertNotNull(tag1BagFile);
		assertTrue(bag.getTags().contains(tag1BagFile));
		
	}


}