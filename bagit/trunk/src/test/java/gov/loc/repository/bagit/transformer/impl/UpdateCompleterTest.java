package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.bag.LoggingProgressListener;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateCompleterTest {

	BagFactory bagFactory = new BagFactory();
	UpdateCompleter completer;	
	File bagFile;
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	
	private File createTestBag() throws Exception {
		File sourceBagDir = ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag", BagFactory.LATEST.toString().toLowerCase()));
		File testBagDir = new File(sourceBagDir.getParentFile(), "test_bag");
		if (testBagDir.exists()) {
			FileUtils.forceDelete(testBagDir);
		}
		FileUtils.copyDirectory(sourceBagDir, testBagDir);
		/*
		if (deleteTagManifest) {
			File tagManifestFile = new File(testBagDir, ManifestHelper.getTagManifestFilename(Algorithm.MD5, bagFactory.getBagConstants()));
			FileUtils.forceDelete(tagManifestFile);
		}
		*/
		return testBagDir;
	}

	
	@Before
	public void setup() throws Exception {
		completer = new UpdateCompleter(this.bagFactory);
		completer.addProgressListener(new LoggingProgressListener());
		bagFile = this.createTestBag();
		//Add a payload file
		//Delete a payload file
		//Well, actually I'll just rename a file
		File file1 = new File(bagFile, "data/test1.txt");
		file1.renameTo(new File(bagFile, "data/xtest1.txt"));
		//Change a payload file
		File file2 = new File(bagFile, "data/test2.txt");
		FileWriter writer1 = new FileWriter(file2);
		writer1.append("x");
		writer1.close();
		
		//Add a tag
		File file3 = new File(bagFile, "newtag.txt");
		FileWriter writer2 = new FileWriter(file3);
		writer2.append("newtag");
		writer2.close();
				
		
	}

	@Test
	public void testComplete() throws Exception {
		Bag bag = this.bagFactory.createBag(bagFile, LoadOption.BY_PAYLOAD_FILES);
		assertFalse(bag.verifyValid().isSuccess());
		Bag newBag = completer.complete(bag);
		SimpleResult result = newBag.verifyValid();
		System.out.println("X:" + result);
		assertTrue(result.isSuccess());
		BagInfoTxt bagInfoTxt = newBag.getBagInfoTxt();
		//Original doesn't have payload-oxum, so neither should completed
		assertNull(bagInfoTxt.getPayloadOxum());
		assertEquals(this.dateFormat.format(new Date()), bagInfoTxt.getBaggingDate());
//		assertEquals("1 KB", bagInfoTxt.getBagSize());
	}

	@Test
	public void testCompleteWithoutBagInfoTxt() throws Exception {
		File bagInfoTxtFile = new File(bagFile, bagFactory.getBagConstants().getBagInfoTxt());
		FileUtils.forceDelete(bagInfoTxtFile);

		Bag bag = this.bagFactory.createBag(bagFile, LoadOption.BY_PAYLOAD_FILES);
		assertFalse(bag.verifyValid().isSuccess());
		Bag newBag = completer.complete(bag);
		SimpleResult result = newBag.verifyValid();
		assertTrue(result.isSuccess());
		assertNull(newBag.getBagInfoTxt());
	}

}