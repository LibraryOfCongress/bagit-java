package gov.loc.repository.bagit.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;

public class AddFilesToPayloadOperationTest {
	
	File destFile;
    static Integer counter = 0;
    AddFilesToPayloadOperation driver;
	BagFactory bagFactory; 
	BagPartFactory bagPartFactory;
	
	@Before
	public void setup() throws Exception{
		this.bagFactory = new BagFactory();
		this.bagPartFactory = bagFactory.getBagPartFactory(this.getVersion());
	}
	
	public Version getVersion() {
		return Version.V0_96;
	}
	
	@Test
	public void testBagByAddingPayloadFiles() throws Exception{
		File sourceBagDir = ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag", this.getVersion().toString().toLowerCase()));
		File srcAddFilesDir = new File(ResourceHelper.PROJECT_DIR+"/src/test/resources/srcFiles");
		//Read Bag from disk
		Bag bag = this.bagFactory.createBag(sourceBagDir);

		bag.addFilesToPayload(Arrays.asList(srcAddFilesDir.listFiles()));
		
//		AddFilesToPayloadOperation driver = new AddFilesToPayloadOperation(bag);
//		if (srcAddFilesDir.isDirectory()) {
//				bag = driver.addFilesToPayload(Arrays.asList(srcAddFilesDir.listFiles()));
//		}

		DefaultCompleter completer = new DefaultCompleter(bagFactory);
		bag = completer.complete(bag);
		
        assertTrue(bag.verifyValid().isSuccess());
        System.out.println(bag.getPayload().size());
        assertEquals(11, bag.getPayload().size());
        assertTrue(bag.verifyValid().isSuccess());
        BagInfoTxt bagInfo = bag.getBagInfoTxt();
        assertNotNull(bagInfo);
        assertNotNull(bagInfo.getBaggingDate());
        assertNotNull(bagInfo.getBagSize());
        assertNotNull(bagInfo.getPayloadOxum());
        assertEquals(1, bag.getTagManifests().size());
		
	}
	
}
