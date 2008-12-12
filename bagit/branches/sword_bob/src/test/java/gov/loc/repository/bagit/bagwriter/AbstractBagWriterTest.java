package gov.loc.repository.bagit.bagwriter;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractBagWriterTest {

	@Before
	public void setUp() throws Exception {
		if (this.getBagFile().exists()) {
			FileUtils.forceDelete(this.getBagFile());
		}
	}

	public abstract BagWriter getBagWriter();
	
	public abstract File getBagFile();
	
	@Test
	public void testWriter() throws Exception {
		Bag bag = BagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest"));
		assertTrue(bag.isValid().isSuccess());
		bag.write(this.getBagWriter());
		
		assertTrue(this.getBagFile().exists());
		Bag newBag = BagFactory.createBag(this.getBagFile());
		assertTrue(newBag.isValid().isSuccess());
		
		List<Manifest> payloadManifests = newBag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals(4, newBag.getTagFiles().size());
		assertNotNull(newBag.getTagFile("bagit.txt"));
		
		assertEquals(5, newBag.getPayloadFiles().size());
		assertNotNull(newBag.getPayloadFile("data/dir1/test3.txt"));
		
	}

}
