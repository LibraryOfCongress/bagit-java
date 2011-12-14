package gov.loc.repository.bagit.writer.impl;

import static org.junit.Assert.*;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.bag.CancelTriggeringBagDecorator;
import gov.loc.repository.bagit.progresslistener.PrintingProgressListener;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractWriterTest {

	BagFactory bagFactory = new BagFactory();
	
	@Before
	public void setUp() throws Exception {
		if (this.getBagFile().exists()) {
			FileUtils.forceDelete(this.getBagFile());
		}
	}

	public abstract Writer getBagWriter();
	
	public abstract File getBagFile();
	
	@Test
	public void testWriter() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		try {
			assertTrue(bag.verifyValid().isSuccess());
			Writer writer = this.getBagWriter();
			writer.addProgressListener(new PrintingProgressListener());
			
			Bag newBag = writer.write(bag, this.getBagFile());
			try {
				assertNotNull(newBag);
				assertTrue(this.getBagFile().exists());
				assertTrue(newBag.verifyValid().isSuccess());
				
				List<Manifest> payloadManifests = newBag.getPayloadManifests();
				assertEquals(1, payloadManifests.size());
				assertEquals("manifest-md5.txt", payloadManifests.get(0).getFilepath());
				assertEquals(4, newBag.getTags().size());
				assertNotNull(newBag.getBagFile("bagit.txt"));
				
				assertEquals(5, newBag.getPayload().size());
				assertNotNull(newBag.getBagFile("data/dir1/test3.txt"));
			} finally {
				newBag.close();
			}
		} finally {
			bag.close();
		}
		
	}

	@Test
	public void testCancel() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		try {
			assertTrue(bag.verifyValid().isSuccess());
			
			Writer writer = this.getBagWriter();
					
			Bag newBag = writer.write(new CancelTriggeringBagDecorator(bag, 10, writer), this.getBagFile());
			try {
				assertNull(newBag);
			} finally {
				bag.close();
			}
		} finally {
			bag.close();
		}
	}
	
	@Test
	public void testOverwrite() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		Bag newBag = null;
		Bag newestBag = null;
		try {
			Writer writer = this.getBagWriter();
			
			newBag = writer.write(bag, this.getBagFile());
			assertTrue(newBag.verifyValid().isSuccess());
	
			//OK, now write the bag again
			newestBag = writer.write(newBag, this.getBagFile());
			assertTrue(newestBag.verifyValid().isSuccess());
		} finally { 
			bag.close();
			if (newBag != null) newBag.close();
			if (newestBag != null) newestBag.close();
		}
		
	}

}
