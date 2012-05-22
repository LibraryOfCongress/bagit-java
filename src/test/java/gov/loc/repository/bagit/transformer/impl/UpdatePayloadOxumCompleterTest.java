package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

public class UpdatePayloadOxumCompleterTest {

	BagFactory bagFactory = new BagFactory();
	UpdatePayloadOxumCompleter completer;	
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
		completer = new UpdatePayloadOxumCompleter(this.bagFactory);
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
		Bag newBag = null;
		Bag newBag2 = null;
		
		try {
			//There is no payload-oxum
			assertNull(bag.getBagInfoTxt().getPayloadOxum());
			newBag = completer.complete(bag);
			//Not added if there 
			assertNull(newBag.getBagInfoTxt().getPayloadOxum());
			
			newBag.getBagInfoTxt().setPayloadOxum(2, 3);
			assertFalse(comparePayloadOxum(newBag));
			newBag2 = completer.complete(newBag);
			assertTrue(comparePayloadOxum(newBag2));
			
		} finally {
			if (newBag != null) newBag.close();
			if (newBag2 != null) newBag2.close();
		}

	}

	private boolean comparePayloadOxum(Bag bag) {
		String genOxum = BagHelper.generatePayloadOxum(bag);
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		String checkOxum = bagInfo.getPayloadOxum();
		return genOxum.equals(checkOxum);
		
	}

	
}