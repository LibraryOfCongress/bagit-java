package gov.loc.repository.bagit.driver;

import static org.junit.Assert.*;

import java.io.File;
import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import static gov.loc.repository.bagit.driver.CommandLineBagDriver.*;

public class CommandLineBagDriverTest {

	File destFile;
    static Integer counter = 0;
	CommandLineBagDriver driver;
	BagFactory bagFactory = new BagFactory();
    
	@Before
	public void setup() throws Exception {
		counter++;
        destFile = new File(ResourceHelper.getFile("bags"), MessageFormat.format("foo{0}.zip", counter));
        if (destFile.exists()) {
			FileUtils.forceDelete(destFile);
		}
        driver = new CommandLineBagDriver();
	}
	
	@Test
	public void testNoArgs() throws Exception {
		assertEquals(RETURN_SUCCESS, driver.execute(new String[] {}));
	}

	@Test
	public void testMissingArgs() throws Exception {
		assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_VERIFYVALID}));
	}
	
	@Test
	public void testIsValid() throws Exception {
		assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_VERIFYVALID, ResourceHelper.getFile("bags/v0_95/bag").getAbsolutePath()}));
	}

	@Test
	public void testIsComplete() throws Exception {
		assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_VERIFYCOMPLETE, ResourceHelper.getFile("bags/v0_95/bag").getAbsolutePath()}));
		
	}

	@Test
	public void testverifyTagManifests() throws Exception {
		assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_VERIFY_TAGMANIFESTS, ResourceHelper.getFile("bags/v0_95/bag").getAbsolutePath()}));
	}

	@Test
	public void testverifyPayloadManifests() throws Exception {
		assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_VERIFY_PAYLOADMANIFESTS, ResourceHelper.getFile("bags/v0_95/bag").getAbsolutePath()}));
	}
		
	@Test
	public void testCreate() throws Exception {
        assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir1").getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir2").getAbsolutePath(), "--" + PARAM_WRITER, VALUE_WRITER_ZIP, "--" + PARAM_TAG_MANIFEST_ALGORITHM, Manifest.Algorithm.SHA1.bagItAlgorithm }));
        assertTrue(destFile.exists());
        Bag bag = this.bagFactory.createBag(destFile);
        assertEquals(3, bag.getPayload().size());
        assertTrue(bag.verifyValid().isSuccess());
        BagInfoTxt bagInfo = bag.getBagInfoTxt();
        assertNotNull(bagInfo);
        assertNotNull(bagInfo.getBaggingDate());
        assertNotNull(bagInfo.getBagSize());
        assertNotNull(bagInfo.getPayloadOxum());
        assertEquals(1, bag.getTagManifests().size());
        assertEquals(Manifest.Algorithm.SHA1, bag.getTagManifests().get(0).getAlgorithm());
	}

	@Test
	public void testCreateWithWildcard() throws Exception {
        assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data").getAbsolutePath() + File.separator + "*", "--" + PARAM_WRITER, VALUE_WRITER_ZIP, "--" + PARAM_TAG_MANIFEST_ALGORITHM, Manifest.Algorithm.SHA1.bagItAlgorithm }));
        Bag bag = this.bagFactory.createBag(destFile);
        assertEquals(5, bag.getPayload().size());
        assertTrue(bag.verifyValid().isSuccess());
        assertNotNull(bag.getBagFile("data/test1.txt"));
	}

	
	@Test
	public void testCreateExcludeBagInfoAndTagManifest() throws Exception {
        assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir1").getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir2").getAbsolutePath(), "--" + PARAM_WRITER, VALUE_WRITER_ZIP, "--" + PARAM_EXCLUDE_BAG_INFO, "--" + PARAM_EXCLUDE_TAG_MANIFEST }));
        Bag bag = this.bagFactory.createBag(destFile);
        assertEquals(3, bag.getPayload().size());
        assertTrue(bag.verifyValid().isSuccess());
        assertNull(bag.getBagInfoTxt());
        assertTrue(bag.getTagManifests().isEmpty());
	}

	
	@Test
	public void testCreateWithMissingFile() throws Exception {
        assertEquals(RETURN_ERROR, driver.execute(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir1").getAbsolutePath(), new File("foo").getAbsolutePath(), "--" + PARAM_WRITER, VALUE_WRITER_ZIP}));
	}
	
	@Test
	public void testCreateIncludeDelimiter() throws Exception {
        assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir1").getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir2").getAbsolutePath(), "--" + PARAM_WRITER, VALUE_WRITER_ZIP, "--" + PARAM_MANIFEST_SEPARATOR, "    \t\t" }));
        Bag bag = this.bagFactory.createBag(destFile);
        assertEquals(3, bag.getPayload().size());
        assertTrue(bag.verifyValid().isSuccess());
        BagInfoTxt bagInfo = bag.getBagInfoTxt();
        assertNotNull(bagInfo);
        assertNotNull(bagInfo.getBaggingDate());
        assertNotNull(bagInfo.getBagSize());
        assertNotNull(bagInfo.getPayloadOxum());
        assertEquals(1, bag.getTagManifests().size());
	}

	
	@Test
	public void testMakeHoley() throws Exception {
		final String BASE_URL = "http://foo.com/bag";
        assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_MAKE_HOLEY, ResourceHelper.getFile("bags/v0_95/bag").getAbsolutePath(), destFile.getAbsolutePath(), BASE_URL, "--" + PARAM_WRITER, VALUE_WRITER_ZIP}));
        assertTrue(destFile.exists());
        Bag bag = this.bagFactory.createBag(destFile);
        FetchTxt fetch = bag.getFetchTxt();
        assertNotNull(fetch);
        assertTrue(fetch.get(0).getUrl().startsWith(BASE_URL));
	}
	
	
	@Test
	public void testUpdateIncludeDelimiter() throws Exception {
	    assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_CREATE, destFile.getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir1").getAbsolutePath(), ResourceHelper.getFile("bags/v0_95/bag/data/dir2").getAbsolutePath()}));
	    assertEquals(RETURN_SUCCESS, driver.execute(new String[] {OPERATION_UPDATE, destFile.getAbsolutePath(), "--" + PARAM_MANIFEST_SEPARATOR, "    \t\t" }));
        Bag bag = this.bagFactory.createBag(destFile);
        assertEquals(3, bag.getPayload().size());
        assertTrue(bag.verifyValid().isSuccess());
        BagInfoTxt bagInfo = bag.getBagInfoTxt();
        assertNotNull(bagInfo);
        assertNotNull(bagInfo.getBaggingDate());
        assertNotNull(bagInfo.getBagSize());
        assertNotNull(bagInfo.getPayloadOxum());
        assertEquals(1, bag.getTagManifests().size());
	}

}
