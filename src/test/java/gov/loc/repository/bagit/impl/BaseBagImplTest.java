package gov.loc.repository.bagit.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.Before;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;


public abstract class BaseBagImplTest {

	public abstract Version getVersion();
	
	BagFactory bagFactory = new BagFactory();
	BagPartFactory factory;
	BagConstants constants;
	
	@Before
	public void setup() {
		this.factory = bagFactory.getBagPartFactory(this.getVersion());
		this.constants = bagFactory.getBagConstants(this.getVersion());		
	}
	
	protected Bag getBagByPayloadManifests(Format format) throws Exception {
		return this.getBagByPayloadManifests(this.getVersion(), format);  
	}

	protected Bag getBagByPayloadFiles(Format format) throws Exception {
		return this.getBagByPayloadFiles(this.getVersion(), format);  
	}
	
	protected Bag getBagByPayloadManifests(Version version, Bag.Format format) throws Exception {
		return this.bagFactory.createBag(this.getBagDir(version, format), version, LoadOption.BY_MANIFESTS);  
	}	

	protected Bag getBagByPayloadFiles(Version version, Bag.Format format) throws Exception {
		return this.bagFactory.createBag(this.getBagDir(version, format), version, LoadOption.BY_FILES);  
	}	

	
	protected File getBagDir(Version version, Bag.Format format) throws Exception {
		String versionString = version.toString().toLowerCase();
		String extension = format.extension;
		String bagName = this.getBagName();
		
		return ResourceHelper.getFile(MessageFormat.format("bags/{0}/{1}{2}", versionString, bagName, extension));		
	}
	
	protected String getBagName() {
		return "bag";
	}
	
	protected File createTestBag() throws Exception {
		return this.createTestBag(true);
	}

	protected File createTestBag(boolean deleteTagManifest) throws Exception {
		File sourceBagDir = ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag", this.getVersion().toString().toLowerCase()));
		File testBagDir = new File(sourceBagDir.getParentFile(), "test_bag");
		if (testBagDir.exists()) {
			FileUtils.forceDelete(testBagDir);
		}
		FileUtils.copyDirectory(sourceBagDir, testBagDir);
		if (deleteTagManifest) {
			File tagManifestFile = new File(testBagDir, ManifestHelper.getTagManifestFilename(Algorithm.MD5, this.constants));
			FileUtils.forceDelete(tagManifestFile);
		}
		return testBagDir;
	}
	
	protected void testBag(Bag bag) throws Exception
	{
		this.assertBagPayloadManifests(bag);
		this.assertBagTagManifests(bag);
		this.assertBagTagFiles(bag);
		this.assertBagPayloadFiles(bag);		
		this.assertBagDeclaration(bag);		
		this.assertBagInfo(bag);
		this.assertBagComplete(bag);
		this.assertBagValid(bag);
		this.assertBagVerifyTagManifests(bag);
		this.assertBagVerifyPayloadManifests(bag);
	}

	protected void assertBagVerifyPayloadManifests(Bag bag) throws Exception
	{
		assertTrue("Verify Payload Manifests", bag.verifyPayloadManifests().isSuccess());
	}

	protected void assertBagVerifyTagManifests(Bag bag) throws Exception
	{
		assertTrue("Verify Tag Manifests", bag.verifyTagManifests().isSuccess());
	}

	protected void assertBagValid(Bag bag) throws Exception
	{
		assertTrue("Verify Valid", bag.verifyValid().isSuccess());
	}

	protected void assertBagComplete(Bag bag) throws Exception
	{
		assertTrue("Verify Complete", bag.verifyComplete().isSuccess());
	}

	protected void assertBagInfo(Bag bag) throws Exception
	{
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		assertEquals("Spengler University", bagInfo.getSourceOrganization());
	}

	protected void assertBagDeclaration(Bag bag) throws Exception
	{
		BagItTxt bagIt = bag.getBagItTxt();
		assertEquals("UTF-8", bagIt.getCharacterEncoding());
		assertEquals(this.getVersion().versionString, bagIt.getVersion());
	}

	protected void assertBagPayloadFiles(Bag bag) throws Exception
	{
		assertEquals(5, bag.getPayload().size());
		assertNotNull(bag.getBagFile("data/dir1/test3.txt"));
		assertNull(bag.getBagFile("xdata/dir1/test3.txt"));
	}

	protected void assertBagTagFiles(Bag bag) throws Exception
	{
		assertEquals(4, bag.getTags().size());
		assertNotNull(bag.getBagFile("bagit.txt"));
		assertNull(bag.getBagFile("xbagit.txt"));
	}

	protected void assertBagTagManifests(Bag bag) throws Exception
	{
		List<Manifest> tagManifests = bag.getTagManifests();
		assertEquals(1, tagManifests.size());
		assertEquals("tagmanifest-md5.txt", tagManifests.get(0).getFilepath());
		assertEquals("tagmanifest-md5.txt", bag.getTagManifest(Algorithm.MD5).getFilepath());
		assertNull(bag.getTagManifest(Algorithm.SHA1));
		assertNotNull(bag.getChecksums("bagit.txt").get(Algorithm.MD5));
	}

	protected void assertBagPayloadManifests(Bag bag) throws Exception
	{
		List<Manifest> payloadManifests = bag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals("manifest-md5.txt", bag.getPayloadManifest(Algorithm.MD5).getFilepath());
		assertNull(bag.getPayloadManifest(Algorithm.SHA1));
		assertEquals("ad0234829205b9033196ba818f7a872b", bag.getChecksums("data/test2.txt").get(Algorithm.MD5));
	}
	
}
