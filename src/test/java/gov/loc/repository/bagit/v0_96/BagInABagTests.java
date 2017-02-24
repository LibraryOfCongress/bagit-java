package gov.loc.repository.bagit.v0_96;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import org.junit.Test;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.impl.AbstractBagImplTest;

public class BagInABagTests extends AbstractBagImplTest {

	@Override
	public Version getVersion() {
		return Version.V0_96;
	}
	
	@Override
	protected String getBagName() {
		return "bag-in-a-bag";
	};
	
	@Test
	@Override
	public void testRemoveDirectory() throws Exception
	{
		Bag bag = this.getBagByPayloadManifests(Format.FILESYSTEM);
		try {
			assertNotNull(bag.getBagFile("data/bag/data/test1.txt"));
			assertNotNull(bag.getBagFile("data/bag/data/dir2/test4.txt"));
			assertNotNull(bag.getBagFile("data/bag/data/dir2/dir3/test5.txt"));
			
			bag.removePayloadDirectory("data/bag/data/dir2");
			assertNotNull(bag.getBagFile("data/bag/data/test1.txt"));
			assertNull(bag.getBagFile("data/bag/data/dir2/test4.txt"));
			assertNull(bag.getBagFile("data/bag/data/dir2/dir3/test5.txt"));
	
			bag.removePayloadDirectory("data/bag/data/test1.txt");
			assertNotNull(bag.getBagFile("data/bag/data/test1.txt"));
	
			bag.removePayloadDirectory("data");
			assertNotNull(bag.getBagFile("data/bag/manifest-md5.txt"));
		} finally {
			bag.close();
		}
	}
	
	@Override
	protected void assertBagPayloadManifests(Bag bag) throws Exception
	{
		List<Manifest> payloadManifests = bag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals("manifest-md5.txt", bag.getPayloadManifest(Algorithm.MD5).getFilepath());
		assertNull(bag.getPayloadManifest(Algorithm.SHA1));
		assertEquals("ad0234829205b9033196ba818f7a872b", bag.getChecksums("data/bag/data/test2.txt").get(Algorithm.MD5));
		assertEquals("ba8644f8c8b7adb3d5cf3ad4245606e8", bag.getChecksums("data/bag/manifest-md5.txt").get(Algorithm.MD5));
	}
	
	@Override
	protected void assertBagPayloadFiles(Bag bag) throws Exception
	{
		assertEquals(9, bag.getPayload().size());
		assertNotNull(bag.getBagFile("data/bag/data/dir1/test3.txt"));
		assertNull(bag.getBagFile("xdata/bag/data/dir1/test3.txt"));
		assertNotNull(bag.getBagFile("data/bag/manifest-md5.txt"));
		assertNull(bag.getBagFile("data/bag/manifest-sha42.txt"));
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
