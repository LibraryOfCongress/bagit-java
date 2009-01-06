package gov.loc.repository.bagit.driver;

import static org.junit.Assert.*;

import java.io.File;
import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import static gov.loc.repository.bagit.driver.CommandLineBagDriver.*;

public class CommandLineBagDriverTest {

	File destFile;
    static Integer counter = 0;
	
	@Before
	public void setup() throws Exception {
		counter++;
        destFile = new File(ResourceHelper.getFile("bags"), MessageFormat.format("foo{0}.zip", counter));
        if (destFile.exists()) {
			FileUtils.forceDelete(destFile);
		}
	}
	
	@Test
	public void testNoArgs() throws Exception {
		assertEquals(RETURN_ERROR, CommandLineBagDriver.main2(new String[] {}));
	}

	@Test
	public void testMissingArgs() throws Exception {
		assertEquals(RETURN_ERROR, CommandLineBagDriver.main2(new String[] {OPERATION_ISVALID}));
	}
	
	@Test
	public void testIsValid() throws Exception {
		assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_ISVALID, ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest").getAbsolutePath()}));
		assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_ISVALID, ResourceHelper.getFile("bags/v0_95/bag_with_no_bagit").getAbsolutePath(), "--missingbagittolerant", "-v", "0.95"}));
		assertEquals(RETURN_FAILURE, CommandLineBagDriver.main2(new String[] {OPERATION_ISVALID, ResourceHelper.getFile("bags/v0_95/invalid_bag_with_one_manifest").getAbsolutePath()}));
	}

	@Test
	public void testIsComplete() throws Exception {
		assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_ISCOMPLETE, ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest").getAbsolutePath()}));
		assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_ISCOMPLETE, ResourceHelper.getFile("bags/v0_95/bag_with_no_bagit").getAbsolutePath(), "--missingbagittolerant", "-v", "0.95"}));
		assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_ISCOMPLETE, ResourceHelper.getFile("bags/v0_95/invalid_bag_with_one_manifest").getAbsolutePath()}));
		
	}
	
	@Test
	public void testWrite() throws Exception {
        System.out.println(ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest").getAbsolutePath()); 
        assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_WRITE, ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest").getAbsolutePath(), "-d", destFile.getAbsolutePath(), "-w", VALUE_WRITER_ZIP}));
        assertTrue(destFile.exists());
        Bag bag = BagFactory.createBag(destFile);
        assertEquals(Format.ZIP, bag.getFormat());
        assertTrue(bag.isValid().isSuccess());		
	}
	
	@Test
	public void testBadWriter() throws Exception {
        assertEquals(RETURN_ERROR, CommandLineBagDriver.main2(new String[] {OPERATION_WRITE, ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest").getAbsolutePath(), "-d", destFile.getAbsolutePath(), "-w", "foozip"}));
	}
	
	@Test
	public void testCreate() throws Exception {
        assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_CREATE, "-d", destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir1").getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir2").getAbsolutePath(), "-w", VALUE_WRITER_ZIP, "-t", Manifest.Algorithm.SHA1.bagItAlgorithm }));
        Bag bag = BagFactory.createBag(destFile);
        assertEquals(3, bag.getPayloadFiles().size());
        assertTrue(bag.isValid().isSuccess());
        BagInfoTxt bagInfo = bag.getBagInfoTxt();
        assertNotNull(bagInfo);
        assertNotNull(bagInfo.getBaggingDate());
        assertNotNull(bagInfo.getBagSize());
        assertNotNull(bagInfo.getPayloadOxum());
        assertEquals(1, bag.getTagManifests().size());
        assertEquals(Manifest.Algorithm.SHA1, bag.getTagManifests().get(0).getAlgorithm());
        
	}

	@Test
	public void testCreateExcludeBagInfoAndTagManifest() throws Exception {
        assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_CREATE, "-d", destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir1").getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir2").getAbsolutePath(), "-w", VALUE_WRITER_ZIP, "--excludebaginfo", "--excludetagmanifest" }));
        Bag bag = BagFactory.createBag(destFile);
        assertEquals(3, bag.getPayloadFiles().size());
        assertTrue(bag.isValid().isSuccess());
        assertNull(bag.getBagInfoTxt());
        assertTrue(bag.getTagManifests().isEmpty());
	}

	
	@Test
	public void testCreateWithMissingFile() throws Exception {
        assertEquals(RETURN_ERROR, CommandLineBagDriver.main2(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest/data/dir1").getAbsolutePath(), new File("foo").getAbsolutePath(), "-w", VALUE_WRITER_ZIP}));
	}
	
	@Test
	public void testMakeHoley() throws Exception {
		final String BASE_URL = "http://foo.com/bag";
        assertEquals(RETURN_SUCCESS, CommandLineBagDriver.main2(new String[] {OPERATION_MAKE_HOLEY, ResourceHelper.getFile("bags/v0_95/bag_with_one_manifest").getAbsolutePath(), "-d", destFile.getAbsolutePath(), BASE_URL, "-w", VALUE_WRITER_ZIP}));
        assertTrue(destFile.exists());
        Bag bag = BagFactory.createBag(destFile);
        FetchTxt fetch = bag.getFetchTxt();
        assertNotNull(fetch);
        assertTrue(fetch.get(0).getUrl().startsWith(BASE_URL));
	}
	
}
