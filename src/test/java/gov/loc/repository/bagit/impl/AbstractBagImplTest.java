package gov.loc.repository.bagit.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagInfoTxtWriter;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.ManifestWriter;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.bag.CancelTriggeringBagDecorator;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractBagImplTest extends BaseBagImplTest {
	
	@Test
	public void testFileSystemBagByPayloadManifests() throws Exception {
		this.testBag(this.getBagByPayloadManifests(Format.FILESYSTEM));
	}

	@Test
	public void testFileSystemBagByPayloadFiles() throws Exception {
		this.testBag(this.getBagByPayloadFiles(Format.FILESYSTEM));
	}
	
	@Test
	public void testZipBagByPayloadManifests() throws Exception {
		this.testBag(this.getBagByPayloadManifests(Format.ZIP));
	}

	@Test
	public void testZipBagByPayloadFiles() throws Exception {
		this.testBag(this.getBagByPayloadFiles(Format.ZIP));
	}

	@Test
	public void testTarBagByPayloadManifests() throws Exception {
		this.testBag(this.getBagByPayloadManifests(Format.TAR));
	}

	@Test
	public void testTarBagByPayloadFiles() throws Exception {
		this.testBag(this.getBagByPayloadFiles(Format.TAR));
	}

	@Test
	public void testTarGzBagByPayloadManifests() throws Exception {
		this.testBag(this.getBagByPayloadManifests(Format.TAR_GZ));
	}

	@Test
	public void testTarGzBagByPayloadFiles() throws Exception {
		this.testBag(this.getBagByPayloadFiles(Format.TAR_GZ));
	}

	@Test
	public void testTarBz2BagByPayloadManifests() throws Exception {
		this.testBag(this.getBagByPayloadManifests(Format.TAR_BZ2));
	}

	@Test
	public void testTarBz2BagByPayloadFiles() throws Exception {
		this.testBag(this.getBagByPayloadFiles(Format.TAR_BZ2));
	}

	
	@Test
	public void testBagWithTwoEqualManifests() throws Exception {
		File testBagDir = this.createTestBag();
		File sha1ManifestFile = new File(testBagDir, ManifestHelper.getPayloadManifestFilename(Algorithm.SHA1, this.constants));
		ManifestWriter writer = this.factory.createManifestWriter(new FileOutputStream(sha1ManifestFile));
		writer.write("data/dir1/test3.txt", "3ebfa301dc59196f18593c45e519287a23297589");
		writer.write("data/dir2/dir3/test5.txt", "911ddc3b8f9a13b5499b6bc4638a2b4f3f68bf23");
		writer.write("data/dir2/test4.txt", "1ff2b3704aede04eecb51e50ca698efd50a1379b");
		writer.write("data/test1.txt", "b444ac06613fc8d63795be9ad0beaf55011936ac");
		writer.write("data/test2.txt", "109f4b3c50d7b0df729d299bc6f8e9ef9066971f");
		writer.close();
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertEquals(2, bag.getPayloadManifests().size());

		//assertTrue(bag.verifyComplete().isSuccess());
		assertTrue(bag.verifyValid().isSuccess());
		//assertTrue(bag.verifyTagManifests().isSuccess());
		//assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testBagWithTwoUnequalManifests() throws Exception {
		File testBagDir = this.createTestBag();
		File sha1ManifestFile = new File(testBagDir, ManifestHelper.getPayloadManifestFilename(Algorithm.SHA1, this.constants));
		ManifestWriter sha1Writer = this.factory.createManifestWriter(new FileOutputStream(sha1ManifestFile));
		sha1Writer.write("data/dir1/test3.txt", "3ebfa301dc59196f18593c45e519287a23297589");
		sha1Writer.write("data/dir2/dir3/test5.txt", "911ddc3b8f9a13b5499b6bc4638a2b4f3f68bf23");
		sha1Writer.write("data/dir2/test4.txt", "1ff2b3704aede04eecb51e50ca698efd50a1379b");
		sha1Writer.close();
		File md5ManifestFile = new File(testBagDir, ManifestHelper.getPayloadManifestFilename(Algorithm.MD5, this.constants));
		ManifestWriter md5Writer = this.factory.createManifestWriter(new FileOutputStream(md5ManifestFile));
		md5Writer.write("data/dir1/test3.txt", "8ad8757baa8564dc136c1e07507f4a98");
		md5Writer.write("data/test1.txt", "5a105e8b9d40e1329780d62ea2265d8a");
		md5Writer.write("data/test2.txt", "ad0234829205b9033196ba818f7a872b");
		md5Writer.close();

		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertEquals(2, bag.getPayloadManifests().size());

		assertTrue(bag.verifyComplete().isSuccess());
		assertTrue(bag.verifyValid().isSuccess());
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testBagWithNoBagItTxt() throws Exception {
		File testBagDir = this.createTestBag();
		File bagItTxtFile = new File(testBagDir, this.constants.getBagItTxt());
		assertTrue(bagItTxtFile.exists());
		FileUtils.forceDelete(bagItTxtFile);
		assertFalse(bagItTxtFile.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertNull(bag.getBagItTxt());

		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());				
		
		CompleteVerifier completeVerifier = new CompleteVerifierImpl();
		completeVerifier.setMissingBagItTolerant(true);
	
		assertTrue(completeVerifier.verify(bag).isSuccess());

		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());
		
	}
	
	@Test
	public void testBagWithChangedPayloadFile() throws Exception {
		File testBagDir = this.createTestBag();
		File test1File = new File(testBagDir, "data/test1.txt");
		assertTrue(test1File.exists());
		FileWriter writer = new FileWriter(test1File);
		writer.write("xtest1");
		writer.close();
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);

		assertTrue(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertFalse(bag.verifyPayloadManifests().isSuccess());
		
	}

	@Test
	public void testBagWithChangedTagFile() throws Exception {
		File testBagDir = this.createTestBag(false);
		File bagInfoTxtFile = new File(testBagDir, this.constants.getBagInfoTxt());
		BagInfoTxtWriter writer = this.factory.createBagInfoTxtWriter(new FileOutputStream(bagInfoTxtFile), this.constants.getBagEncoding());
		writer.write("foo", "bar");
		writer.close();
		assertTrue(bagInfoTxtFile.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);

		assertTrue(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertFalse(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());
		
	}
	
	
	@Test
	public void testBagWithMissingPayloadFile() throws Exception {
		File testBagDir = this.createTestBag();
		File test1File = new File(testBagDir, "data/test1.txt");
		assertTrue(test1File.exists());
		FileUtils.forceDelete(test1File);
		assertFalse(test1File.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertFalse(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testBagWithMissingTagFile() throws Exception {
		File testBagDir = this.createTestBag(false);
		File bagInfoTxtFile = new File(testBagDir, this.constants.getBagInfoTxt());
		assertTrue(bagInfoTxtFile.exists());
		FileUtils.forceDelete(bagInfoTxtFile);
		assertFalse(bagInfoTxtFile.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertFalse(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testBagWithExtraPayloadFile() throws Exception {
		File testBagDir = this.createTestBag();
		File extraFile = new File(testBagDir, "data/extra.txt");
		FileWriter writer = new FileWriter(extraFile);
		writer.write("extra");
		writer.close();
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);

		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testCompleteBagWithExtraPayloadFile() throws Exception {
		File testBagDir = this.createTestBag();
		File extraFile = new File(testBagDir, "data/extra.txt");
		FileWriter writer = new FileWriter(extraFile);
		writer.write("extra");
		writer.close();
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_FILES);
		bag.makeComplete();
		
		assertTrue(bag.verifyComplete().isSuccess());
		assertTrue(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	
	@Test
	public void testBagWithExtraTagFile() throws Exception {
		File testBagDir = this.createTestBag(false);
		File extraFile = new File(testBagDir, "extra.txt");
		FileWriter writer = new FileWriter(extraFile);
		writer.write("extra");
		writer.close();
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);

		assertTrue(bag.verifyComplete().isSuccess());
		assertTrue(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testBagWithNoPayloadManifests() throws Exception {
		File testBagDir = this.createTestBag();
		File manifestFile = new File(testBagDir, ManifestHelper.getPayloadManifestFilename(Algorithm.MD5, this.constants));
		assertTrue(manifestFile.exists());
		FileUtils.forceDelete(manifestFile);
		assertFalse(manifestFile.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());

	}

	@Test
	public void testBagWithExtraDirectory() throws Exception {
		File testBagDir = this.createTestBag();
		File extraDir = new File(testBagDir, "extra");
		assertFalse(extraDir.exists());
		FileUtils.forceMkdir(extraDir);
		assertTrue(extraDir.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());
		
		CompleteVerifier verifier = new CompleteVerifierImpl();
		verifier.setAdditionalDirectoriesInBagDirTolerant(true);
		assertTrue(bag.verify(verifier).isSuccess());		

	}

	@Test
	public void testBagWithIgnoredExtraDirectory() throws Exception {
		File testBagDir = this.createTestBag();
		File extraDir = new File(testBagDir, "extra");
		assertFalse(extraDir.exists());
		FileUtils.forceMkdir(extraDir);
		assertTrue(extraDir.exists());
		File extraFile = new File(extraDir, "extra.txt");
		FileWriter writer = new FileWriter(extraFile);
		writer.write("extra");
		writer.close();
		assertTrue(extraFile.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertFalse(bag.verifyComplete().isSuccess());
		assertFalse(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());
		
		CompleteVerifier verifier = new CompleteVerifierImpl();
		List<String> ignoreDirs = new ArrayList<String>();
		ignoreDirs.add("extra");
		verifier.setIgnoreAdditionalDirectories(ignoreDirs);
		assertTrue(bag.verify(verifier).isSuccess());		

	}
	
	@Test
	public void testBagWithSpecialCharacters() throws Exception {
		File testBagDir = this.createTestBag();
		File specialCharFile = new File(testBagDir, "data/testü.txt");
		FileWriter writer = new FileWriter(specialCharFile);
		writer.write("test1");
		writer.close();

		File sha1ManifestFile = new File(testBagDir, ManifestHelper.getPayloadManifestFilename(Algorithm.SHA1, this.constants));
		ManifestWriter manifestWriter = this.factory.createManifestWriter(new FileOutputStream(sha1ManifestFile));
		manifestWriter.write("data/testü.txt", "b444ac06613fc8d63795be9ad0beaf55011936ac");
		manifestWriter.close();

		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertTrue(bag.verifyComplete().isSuccess());
		assertTrue(bag.verifyValid().isSuccess());
		
		assertTrue(bag.verifyTagManifests().isSuccess());
		assertTrue(bag.verifyPayloadManifests().isSuccess());
		
	}
	
	@Test
	public void testWrongVersion() throws Exception {
		Version otherVersion = null;
		for(Version checkVersion : Version.values()) {
			if (! this.getVersion().equals(checkVersion)) {
				otherVersion = checkVersion;
			}
		}
		
		//May throw a RuntimeException if contains a \ in manifest
		try {
			File bagDir = this.getBagDir(otherVersion, Bag.Format.FILESYSTEM); 
			
			Bag bag = this.bagFactory.createBag(bagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
			assertFalse(bag.verifyComplete().isSuccess());
			assertFalse(bag.verifyValid().isSuccess());
		} catch (RuntimeException ex) {}

	}
		
	@Test
	public void testCreateBag() throws Exception {
		Bag bag = this.bagFactory.createBag(this.getVersion());
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/dir1", this.getVersion().toString().toLowerCase())));
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/dir2", this.getVersion().toString().toLowerCase())));
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/test1.txt", this.getVersion().toString().toLowerCase())));
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/test2.txt", this.getVersion().toString().toLowerCase())));

		BagInfoTxt bagInfo = bag.getBagPartFactory().createBagInfoTxt();
		bag.putBagFile(bagInfo);
		final String BAG_COUNT = "1 of 5";
		final String BAG_SIZE = "10 gb";
		final String BAGGING_DATE = "10.20.2008";
		bagInfo.setBagCount(BAG_COUNT);
		bagInfo.setBagSize(BAG_SIZE);
		bagInfo.setBaggingDate(BAGGING_DATE);
		
		assertEquals(5, bag.getPayload().size());
		assertNotNull(bag.getBagFile("data/dir1/test3.txt"));
		assertNotNull(bag.getBagFile("data/test1.txt"));
		assertNull(bag.getBagFile("xdata/dir1/test3.txt"));
		assertNotNull(bag.getBagInfoTxt());
		assertEquals(BAG_COUNT, bag.getBagInfoTxt().getBagCount());
		assertEquals(BAG_SIZE, bagInfo.getBagSize());
		assertEquals(BAGGING_DATE, bagInfo.getBaggingDate());
		this.addlTestCreateBag(bag);
				
	}
	
	public void addlTestCreateBag(Bag bag){};

	@Test
	public void testCompleterCancels() throws Exception
	{
		Bag bag = this.getBagByPayloadManifests(Format.FILESYSTEM);
		
		CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
		assertNull(completeVerifier.verify(new CancelTriggeringBagDecorator(bag, 10, completeVerifier)));
	}
	
	@Test
	public void testManifestVerifierCancels() throws Exception
	{
		Bag bag = this.getBagByPayloadManifests(Format.FILESYSTEM);
		
		ParallelManifestChecksumVerifier manifestVerifier = new ParallelManifestChecksumVerifier();
		assertNull(manifestVerifier.verify(bag.getPayloadManifests(), new CancelTriggeringBagDecorator(bag, 2, manifestVerifier)));
	}
	
	@Test
	public void testValidVerifierCancels() throws Exception
	{
		Bag bag = this.getBagByPayloadManifests(Format.FILESYSTEM);
		
		ValidVerifierImpl validVerifier = new ValidVerifierImpl(new CompleteVerifierImpl(), new ParallelManifestChecksumVerifier());
		assertNull(validVerifier.verify(new CancelTriggeringBagDecorator(bag, 10, validVerifier)));
	}
		
	@Test
	public void testRemoveDirectory() throws Exception {
		Bag bag = this.getBagByPayloadManifests(Format.FILESYSTEM);
		
		assertNotNull(bag.getBagFile("data/test1.txt"));
		assertNotNull(bag.getBagFile("data/dir2/test4.txt"));
		assertNotNull(bag.getBagFile("data/dir2/dir3/test5.txt"));
		
		bag.removePayloadDirectory("data/dir2");
		assertNotNull(bag.getBagFile("data/test1.txt"));
		assertNull(bag.getBagFile("data/dir2/test4.txt"));
		assertNull(bag.getBagFile("data/dir2/dir3/test5.txt"));

		bag.removePayloadDirectory("data/test1.txt");
		assertNotNull(bag.getBagFile("data/test1.txt"));

		bag.removePayloadDirectory("data");
		assertNotNull(bag.getBagFile("data/test1.txt"));

	}

	@Test
	public void testBagWithDupeFieldsInBagInfoTxt() throws Exception {
		File testBagDir = this.createTestBag(true);
		File bagInfoTxtFile = new File(testBagDir, this.constants.getBagInfoTxt());
		BagInfoTxtWriter writer = this.factory.createBagInfoTxtWriter(new FileOutputStream(bagInfoTxtFile), this.constants.getBagEncoding());
		writer.write("Foo", "test1");
		writer.write("Foo", "test2");
		writer.close();
		assertTrue(bagInfoTxtFile.exists());
		
		Bag bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);

		BagInfoTxt bagInfoTxt = bag.getBagInfoTxt();
		assertEquals(2, bagInfoTxt.getList("Foo").size());
		List<String> values = new ArrayList<String>();
		values.add("test1");
		values.add("test2");
		bagInfoTxt.putList("Bar", values);
		
		bag.write(new FileSystemWriter(bagFactory), testBagDir);
		
		bag = this.bagFactory.createBag(testBagDir, this.getVersion(), LoadOption.BY_PAYLOAD_MANIFESTS);
		assertEquals(2, bagInfoTxt.getList("Foo").size());
		assertEquals(2, bagInfoTxt.getList("Bar").size());
		
	}

	
}
