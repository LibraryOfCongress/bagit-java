package gov.loc.repository.bagit.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;

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
		try {
			assertTrue(bag.verifyValid().isSuccess());
		} finally {
			bag.close();
		}
		
	}
	
	@Test
	public void testBagInPlaceRetainingBaseDir() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, true);
		try {
			assertTrue(testDataDir.exists());
			File baseDir = new File(testDataDir, "test_bag");
			assertTrue(baseDir.exists());
			assertTrue(bag.verifyValid().isSuccess());
		} finally {
			bag.close();
		}
		
	}
	
	@Test
	public void testBagInPlaceNotRetainingBaseDir() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
		try {
			assertTrue(testDataDir.exists());
			File baseDir = new File(testDataDir, "test_bag");
			assertFalse(baseDir.exists());
			assertTrue(bag.verifyValid().isSuccess());
		} finally {
			bag.close();
		}
		
	}

	@Test
	public void testBagInPlaceWithIgnoredExtraDir() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		File extraDir = new File(testDir, "extra");
		assertFalse(extraDir.exists());
		FileUtils.forceMkdir(extraDir);
		assertTrue(extraDir.exists());
		File extraFile = new File(extraDir, "extra.txt");
		FileUtils.write(extraFile, "extra");
		assertTrue(extraFile.exists());

		PreBag preBag = bagFactory.createPreBag(testDir);
		List<String> ignoreDirs = new ArrayList<String>();
		ignoreDirs.add("extra");
		preBag.setIgnoreAdditionalDirectories(ignoreDirs);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
		try {
			assertTrue(testDataDir.exists());
			File baseDir = new File(testDataDir, "test_bag");
			assertFalse(baseDir.exists());
			
			CompleteVerifierImpl verifier = new CompleteVerifierImpl();
			verifier.setIgnoreAdditionalDirectories(ignoreDirs);
			assertTrue(verifier.verify(bag).isSuccess());		
	
			assertTrue(extraDir.exists());
			assertTrue(extraFile.exists());
		} finally {
			bag.close();
		}
	}

	@Test(expected=RuntimeException.class)
	public void testBagInPlaceWithDataDirAndTagDirPrev97() throws Exception {
		File testDir = createTestBag(true);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertTrue(testDataDir.exists());
		File extraDir = new File(testDir, "extra");
		assertFalse(extraDir.exists());
		FileUtils.forceMkdir(extraDir);
		assertTrue(extraDir.exists());
		File extraFile = new File(extraDir, "extra.txt");
		FileUtils.write(extraFile, "extra");
		assertTrue(extraFile.exists());

		PreBag preBag = bagFactory.createPreBag(testDir);
		preBag.makeBagInPlace(Version.V0_96, false);
	}

	@Test
	public void testBagInPlaceWithDataDirAndTagDirPostv97() throws Exception {
		File testDir = createTestBag(true);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertTrue(testDataDir.exists());
		File extraDir = new File(testDir, "extra");
		assertFalse(extraDir.exists());
		FileUtils.forceMkdir(extraDir);
		assertTrue(extraDir.exists());
		File extraFile = new File(extraDir, "extra.txt");
		FileUtils.write(extraFile, "extra");
		assertTrue(extraFile.exists());

		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);		
		try {
			assertTrue(testDataDir.exists());
			File baseDir = new File(testDataDir, "test_bag");
			assertFalse(baseDir.exists());
			
			assertTrue(bag.verifyComplete().isSuccess());
	
			assertTrue(extraDir.exists());
			assertTrue(extraFile.exists());
			
			assertNotNull(bag.getBagFile("extra/extra.txt"));
		} finally {
			bag.close();
		}

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
		try {
			assertTrue(testDataDir.exists());
			File movedEmptyDir = new File(testDataDir, "empty");
			assertTrue(movedEmptyDir.exists());
			assertTrue(bag.verifyValid().isSuccess());
		} finally {
			bag.close();
		}
		
	}

	@Test
	public void testBagInPlaceKeepEmptyDirectories() throws Exception {
		File testDir = createTestBag(false);
		assertTrue(testDir.exists());
		File testDataDir = new File(testDir, "data");
		assertFalse(testDataDir.exists());
		File emptyDir = new File(testDir, "empty_dir");
		FileUtils.forceMkdir(emptyDir);
		assertTrue(emptyDir.exists());
		
		PreBag preBag = bagFactory.createPreBag(testDir);
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, true, true);
		try {
			assertTrue(testDataDir.exists());
			File baseDir = new File(testDataDir, "test_bag");
			assertTrue(baseDir.exists());
			File newEmptyDir = new File(baseDir, "empty_dir");
			assertTrue(newEmptyDir.exists());
			assertTrue((new File(newEmptyDir, ".keep").exists()));
			assertTrue(bag.verifyValid().isSuccess());
		} finally {
			bag.close();
		}
		
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
