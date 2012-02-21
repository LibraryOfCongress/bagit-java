package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

public class CompleterHelperTest {

	BagFactory bagFactory = new BagFactory();
	CompleterHelper completerHelper;
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
		completerHelper = new CompleterHelper();
		this.testBagFile = this.createTestBag();
		this.bag = this.bagFactory.createBag(testBagFile);
		assertTrue(this.bag.verifyValid().isSuccess());
	}
	
	@After
	public void cleanup() {
		IOUtils.closeQuietly(bag);
	}

	@Test
	public void testUpdateTagManifestsWithoutCleaning() throws Exception {
		//This was creating a NPE, so testing that fixed
		Manifest newManifest = bag.getBagPartFactory().createManifest(ManifestHelper.getPayloadManifestFilename(Algorithm.SHA256, bag.getBagConstants()));
		File payloadFile = new File(testBagFile, "data/test1.txt");
		newManifest.put("data/test1.txt", MessageDigestHelper.generateFixity(payloadFile, Algorithm.SHA256));
		assertNull(newManifest.originalInputStream());
		bag.putBagFile(newManifest);
		bag.getTagManifest(Algorithm.MD5).put(ManifestHelper.getPayloadManifestFilename(Algorithm.SHA256, bag.getBagConstants()), MessageDigestHelper.generateFixity(newManifest.newInputStream(), Algorithm.MD5));
		
		
		//Regenerate the tag manifests
		for(Manifest manifest : bag.getTagManifests()) {
			completerHelper.regenerateManifest(bag, manifest, true);
		}
		
		assertTrue(this.bag.verifyValid().isSuccess());

	}
	
}