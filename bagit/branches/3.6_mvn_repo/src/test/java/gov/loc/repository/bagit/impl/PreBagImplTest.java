package gov.loc.repository.bagit.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class PreBagImplTest {

	BagFactory bagFactory = new BagFactory();
	
	@Test
	public void testBagInPlaceWithExistingDataDir() throws Exception {
		File testDir = createTestBag(true);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertTrue(testDataDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
		assertTrue(bag.verifyValid().isSuccess());
		
	}
	
	@Test
	public void testBagInPlaceRetainingBaseDir() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, true);
		assertTrue(testDataDir.exists());
		File baseDir = new File(testDataDir, "test_bag");
		assertTrue(baseDir.exists());
		assertTrue(bag.verifyValid().isSuccess());
		
	}
	
	@Test
	public void testBagInPlaceNotRetainingBaseDir() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
		assertTrue(testDataDir.exists());
		File baseDir = new File(testDataDir, "test_bag");
		assertFalse(baseDir.exists());
		assertTrue(bag.verifyValid().isSuccess());
		
	}

	@Test
	public void testBagInPlaceWithEmptyDir() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File emptyDir = new File(testDir, "empty");
		assertTrue(emptyDir.mkdir());
		assertTrue(emptyDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
		assertTrue(testDataDir.exists());
		File movedEmptyDir = new File(testDataDir, "empty");
		assertTrue(movedEmptyDir.exists());
		assertTrue(bag.verifyValid().isSuccess());
		
	}

	
	private File createTestBag(boolean includeDataDirectory) throws Exception {		
		File sourceBagDir = ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag", BagFactory.LATEST.toString().toLowerCase()));
		File sourceDataDir = new File(sourceBagDir, "data");
		File testBagDir = new File(sourceBagDir.getParentFile(), "test_bag");
		if (testBagDir.exists()) {
			FileUtils.forceDelete(testBagDir);
		}		
		if (includeDataDirectory) {
			FileUtils.copyDirectoryToDirectory(sourceDataDir, testBagDir);
		} else {
			System.out.println("Copying " + sourceDataDir + " to " + testBagDir);
			FileUtils.copyDirectory(sourceDataDir, testBagDir);
			assertTrue(testBagDir.exists());

		}
		return testBagDir;
	}

	
}
