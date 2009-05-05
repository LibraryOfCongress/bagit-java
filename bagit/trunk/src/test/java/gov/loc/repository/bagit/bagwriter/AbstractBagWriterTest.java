package gov.loc.repository.bagit.bagwriter;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;

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
		Bag bag = BagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		assertTrue(bag.checkValid().isSuccess());
		Bag newBag = bag.write(this.getBagWriter());
		assertNotNull(newBag);
		assertTrue(this.getBagFile().exists());
		assertTrue(newBag.checkValid().isSuccess());
		
		List<Manifest> payloadManifests = newBag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals(4, newBag.getTags().size());
		assertNotNull(newBag.getBagFile("bagit.txt"));
		
		assertEquals(5, newBag.getPayload().size());
		assertNotNull(newBag.getBagFile("data/dir1/test3.txt"));
		
	}

}
