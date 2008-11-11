package gov.loc.repository.bagit.v0_95;

import static org.junit.Assert.*;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.v0_95.impl.BagImpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class BagImplTest {

	private Bag getBag(String filepath) throws Exception {
		return BagFactory.createBag(ResourceHelper.getFile(filepath), Version.V0_95);  
	}

	private Bag getBag(String filepath, Version version) throws Exception {
		return BagFactory.createBag(ResourceHelper.getFile(filepath), version);  
	}	
	
	@Test
	public void testFileSystemBag() throws Exception {
		Bag bag = this.getBag("bags/v0_95/bag_with_one_manifest");
		assertEquals(Bag.Format.FILESYSTEM, bag.getFormat());
		
		List<Manifest> payloadManifests = bag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals(4, bag.getTagFiles().size());
		assertNotNull(bag.getTagFile("bagit.txt"));
		assertNull(bag.getTagFile("xbagit.txt"));
		
		assertEquals(5, bag.getPayloadFiles().size());
		assertNotNull(bag.getPayloadFile("data/dir1/test3.txt"));
		assertNull(bag.getPayloadFile("xdata/dir1/test3.txt"));
		
		BagItTxt bagIt = bag.getBagItTxt();
		assertEquals("UTF-8", bagIt.getCharacterEncoding());
		assertEquals("0.95", bagIt.getVersion());
		
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		assertEquals("Spengler University", bagInfo.getSourceOrganization());
	}

	@Test
	public void testZipBag() throws Exception {
		Bag bag = this.getBag("bags/v0_95/bag_with_one_manifest.zip");
		assertEquals(Bag.Format.ZIP, bag.getFormat());

		List<Manifest> payloadManifests = bag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals(4, bag.getTagFiles().size());
		assertNotNull(bag.getTagFile("bagit.txt"));
		assertNull(bag.getTagFile("xbagit.txt"));
		
		assertEquals(5, bag.getPayloadFiles().size());
		assertNotNull(bag.getPayloadFile("data/dir1/test3.txt"));
		assertNull(bag.getPayloadFile("xdata/dir1/test3.txt"));

	}

	@Test
	public void testTarBag() throws Exception {
		Bag bag = this.getBag("bags/v0_95/bag_with_one_manifest.tar");
		assertEquals(Bag.Format.TAR, bag.getFormat());

		List<Manifest> payloadManifests = bag.getPayloadManifests();
		assertEquals(1, payloadManifests.size());
		assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
		assertEquals(4, bag.getTagFiles().size());
		assertNotNull(bag.getTagFile("bagit.txt"));
		assertNull(bag.getTagFile("xbagit.txt"));
		
		assertEquals(5, bag.getPayloadFiles().size());
		assertNotNull(bag.getPayloadFile("data/dir1/test3.txt"));
		assertNull(bag.getPayloadFile("xdata/dir1/test3.txt"));
		
	}
	
	@Test
	public void testCompleteBags() throws Exception {
		assertTrue(this.getBag("bags/v0_95/bag_with_one_manifest").isComplete().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_one_manifest.zip").isComplete().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_one_manifest.tar").isComplete().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_two_equal_manifests").isComplete().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_two_unequal_manifests").isComplete().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_special_chars_in_filename").isComplete().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_no_bagit").isComplete(true).isSuccess());
		assertTrue(this.getBag("bags/v0_95/invalid_bag_with_one_manifest").isComplete().isSuccess());
		
		assertFalse(this.getBag("bags/v0_95/bag_with_no_manifests").isComplete().isSuccess());
		assertFalse(this.getBag("bags/v0_95/bag_with_no_bagit").isComplete(false).isSuccess());
		assertFalse(this.getBag("bags/v0_95/bag_with_extra_directory").isComplete(false).isSuccess());
		assertFalse(this.getBag("bags/v0_95/bag_with_extra_files").isComplete(false).isSuccess());
	}

	@Test
	public void testValidBags() throws Exception {
		assertTrue(this.getBag("bags/v0_95/bag_with_one_manifest").isValid().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_one_manifest.zip").isValid().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_one_manifest.tar").isValid().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_two_equal_manifests").isValid().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_two_unequal_manifests").isValid().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_special_chars_in_filename").isValid().isSuccess());
		assertTrue(this.getBag("bags/v0_95/bag_with_no_bagit").isValid(true).isSuccess());
		
		assertFalse(this.getBag("bags/v0_95/invalid_bag_with_one_manifest").isValid().isSuccess());
		assertFalse(this.getBag("bags/v0_95/bag_with_no_data_directory").isValid(false).isSuccess());
		assertFalse(this.getBag("bags/v0_96/bag_with_one_manifest", Version.V0_95).isValid(false).isSuccess());
	}
	
	@Test
	public void testCreateBag() throws Exception {
		Bag bag = new BagImpl();
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir1"));
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir2"));
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/test1.txt"));
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/test2.txt"));

		BagInfoTxt bagInfo = bag.getBagPartFactory().createBagInfoTxt();
		bag.setBagInfoTxt(bagInfo);
		final String BAG_COUNT = "1 of 5";
		final String BAG_SIZE = "10 gb";
		final String BAGGING_DATE = "10.20.2008";
		bagInfo.setBagCount(BAG_COUNT);
		bagInfo.setBagSize(BAG_SIZE);
		bagInfo.setBaggingDate(BAGGING_DATE);
		
		assertEquals(5, bag.getPayloadFiles().size());
		assertNotNull(bag.getPayloadFile("data/dir1/test3.txt"));
		assertNotNull(bag.getPayloadFile("data/test1.txt"));
		assertNull(bag.getPayloadFile("xdata/dir1/test3.txt"));
		assertNotNull(bag.getBagInfoTxt());
		assertEquals(BAG_COUNT, bag.getBagInfoTxt().getBagCount());
		assertEquals(BAG_SIZE, bagInfo.getBagSize());
		assertEquals(bagInfo.getBagSize(), bagInfo.get(gov.loc.repository.bagit.v0_95.impl.BagInfoTxtImpl.PACKAGE_SIZE));
		assertEquals(BAGGING_DATE, bagInfo.getBaggingDate());
		assertEquals(bagInfo.getBaggingDate(), bagInfo.get(gov.loc.repository.bagit.v0_95.impl.BagInfoTxtImpl.PACKING_DATE));
		
		
	}
	
	@Test
	public void testComplete() throws Exception {
		Bag bag = new BagImpl();
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir1"));
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir2"));
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/test1.txt"));
		bag.addPayload(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/test2.txt"));

		assertEquals(5, bag.getPayloadFiles().size());
				
		bag.complete();
		assertTrue(bag.isComplete().isSuccess());
		assertTrue(bag.isValid().isSuccess());

		//Make sure that has BagIt.txt, tag manifest, payload manifest
		BagItTxt bagIt = bag.getBagItTxt();
		assertEquals("UTF-8", bagIt.getCharacterEncoding());
		assertEquals("0.95", bagIt.getVersion());
		
		assertEquals(1, bag.getTagManifests().size());
		assertEquals(1, bag.getPayloadManifests().size());
		
		//Make sure that payload-oxum set correctly
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		assertNotNull(bagInfo);
		assertEquals("25.5", bagInfo.getPayloadOxum());
		assertEquals((new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime()), bagInfo.getBaggingDate());
		assertEquals("0.1 KB", bagInfo.getBagSize());
	}
	
	@Test
	public void testMakeHoley() throws Exception {
		Bag bag = this.getBag("bags/v0_95/bag_with_one_manifest");
		assertEquals(5, bag.getPayloadFiles().size());
		assertNull(bag.getFetchTxt());
		
		bag.makeHoley("http://foo.com/bag", true);
		FetchTxt fetch = bag.getFetchTxt();
		assertNotNull(fetch);
		assertEquals(5, fetch.size());
		FilenameSizeUrl filenameSizeUrl = fetch.get(0);
		assertEquals("data/dir2/dir3/test5.txt", filenameSizeUrl.getFilename());
		assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
		assertEquals("http://foo.com/bag/data/dir2/dir3/test5.txt", filenameSizeUrl.getUrl());
		
		assertEquals(0, bag.getPayloadFiles().size());
		
		bag = this.getBag("bags/v0_95/bag_with_one_manifest");

		bag.makeHoley("http://foo.com/bag/", false);
		fetch = bag.getFetchTxt();
		assertNotNull(fetch);
		filenameSizeUrl = fetch.get(0);
		assertEquals("data/dir2/dir3/test5.txt", filenameSizeUrl.getFilename());
		assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
		assertEquals("http://foo.com/bag/dir2/dir3/test5.txt", filenameSizeUrl.getUrl());
		
	}

}