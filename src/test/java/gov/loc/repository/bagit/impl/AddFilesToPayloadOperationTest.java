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
		File srcAddFilesDir = new File(ResourceHelper.PROJECT_DIR+"/target/test-classes/srcFiles");
		//Read Bag from disk
		Bag bag = this.bagFactory.createBag(sourceBagDir);
		try {
			bag.addFilesToPayload(Arrays.asList(srcAddFilesDir.listFiles()));
			
	//		AddFilesToPayloadOperation driver = new AddFilesToPayloadOperation(bag);
	//		if (srcAddFilesDir.isDirectory()) {
	//				bag = driver.addFilesToPayload(Arrays.asList(srcAddFilesDir.listFiles()));
	//		}
	
			DefaultCompleter completer = new DefaultCompleter(bagFactory);
			Bag bag2 = completer.complete(bag);
			try {
			
		        assertTrue(bag2.verifyValid().isSuccess());
		        assertEquals(7, bag2.getPayload().size());
		        assertTrue(bag2.verifyValid().isSuccess());
		        BagInfoTxt bagInfo = bag2.getBagInfoTxt();
		        assertNotNull(bagInfo);
		        assertNotNull(bagInfo.getBaggingDate());
		        assertNotNull(bagInfo.getBagSize());
		        assertNotNull(bagInfo.getPayloadOxum());
		        assertEquals(1, bag2.getTagManifests().size());
			} finally {
				bag2.close();
			}
		} finally {
			bag.close();
		}
		
	}
	
}
