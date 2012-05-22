package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class TagManifestCompleterTest {

	BagFactory bagFactory = new BagFactory();
	TagManifestCompleter completer;	
	Bag bag;
	File testBagFile;
	
	private File createTestBag() throws Exception {
		File sourceBagDir = ResourceHelper.getFile("bags/v0_96/bag");
		File testBagDir = new File(sourceBagDir.getParentFile(), "test_bag");
		if (testBagDir.exists()) {
			FileUtils.forceDelete(testBagDir);
		}
		FileUtils.copyDirectory(sourceBagDir, testBagDir);
		return testBagDir;
	}

	
	@Before
	public void setup() throws Exception {
		completer = new TagManifestCompleter(this.bagFactory);
		this.testBagFile = this.createTestBag();
		this.bag = this.bagFactory.createBag(testBagFile);
		assertTrue(this.bag.verifyValid().isSuccess());
	}
	
	@After
	public void cleanup() {
		IOUtils.closeQuietly(bag);
	}

	@Test
	public void testComplete() throws Exception {
		Bag newBag = completer.complete(bag);
		Bag newBag2 = null;
		Bag newBag3 = null;
		Bag newBag4 = null;
		
		try {
			assertTrue(newBag.verifyValid().isSuccess());
			//Add a tag
			File test1File = new File(this.testBagFile, "tag.txt");
			assertFalse(test1File.exists());
			FileWriter writer = new FileWriter(test1File);
			writer.write("tag");
			writer.close();
			newBag.addFileAsTag(test1File);
			
			newBag2 = completer.complete(newBag);
			assertTrue(newBag2.verifyValid().isSuccess());
			
			//Change a tag
			writer = new FileWriter(test1File);
			writer.write("xtag");
			writer.close();
			
			newBag3 = completer.complete(newBag2);
			assertTrue(newBag3.verifyValid().isSuccess());
	
			//Remove a tag
			test1File.delete();
			newBag3.removeBagFile("tag.txt");
			
			newBag4 = completer.complete(newBag3);
			assertTrue(newBag4.verifyValid().isSuccess());
		} finally {
			newBag.close();
			if (newBag2 != null) newBag2.close();
			if (newBag3 != null) newBag3.close();
			if (newBag4 != null) newBag4.close();
		}

	}

	@Test
	public void testCompleteWithLimits() throws Exception {
		Bag newBag = completer.complete(bag);
		Bag newBag2 = null;
		Bag newBag3 = null;
		Bag newBag4 = null;
		Bag newBag5 = null;
		Bag newBag6 = null;
		Bag newBag7= null;
		
		try {
			assertTrue(newBag.verifyValid().isSuccess());
			//Add a tag
			File test1File = new File(this.testBagFile, "tag.txt");
			assertFalse(test1File.exists());
			FileWriter writer = new FileWriter(test1File);
			writer.write("tag");
			writer.close();
			newBag.addFileAsTag(test1File);
			
			completer.setLimitAddTagFilepaths(new ArrayList<String>());
			completer.setLimitDeleteTagFilepaths(new ArrayList<String>());
			completer.setLimitUpdateTagFilepaths(new ArrayList<String>());
			
			newBag2 = completer.complete(newBag);
			assertTrue(newBag2.verifyValid().isSuccess());
			assertTrue(newBag2.getChecksums("tag.txt").isEmpty());
			completer.setLimitAddTagFilepaths(Arrays.asList(new String[] {"tag.txt"}));
			newBag3 = completer.complete(newBag);
			assertTrue(newBag3.verifyValid().isSuccess());
			assertFalse(newBag3.getChecksums("tag.txt").isEmpty());
			
			//Change a tag
			writer = new FileWriter(test1File);
			writer.write("xtag");
			writer.close();
			
			newBag4 = completer.complete(newBag2);
			assertFalse(newBag4.verifyValid().isSuccess());
			completer.setLimitUpdateTagFilepaths(Arrays.asList(new String[] {"tag.txt"}));
			newBag5 = completer.complete(newBag2);
			assertTrue(newBag5.verifyValid().isSuccess());
			
	
			//Remove a tag
			test1File.delete();
			newBag4.removeBagFile("tag.txt");
			
			newBag6 = completer.complete(newBag4);
			assertFalse(newBag6.verifyValid().isSuccess());
			completer.setLimitDeleteTagFilepaths(Arrays.asList(new String[] {"tag.txt"}));
			newBag7 = completer.complete(newBag4);
			assertTrue(newBag7.verifyValid().isSuccess());
			
			
		} finally {
			newBag.close();
			if (newBag2 != null) newBag2.close();
			if (newBag3 != null) newBag3.close();
			if (newBag4 != null) newBag4.close();
			if (newBag5 != null) newBag5.close();
			if (newBag6 != null) newBag6.close();
			if (newBag7 != null) newBag7.close();
		}

	}

	
}