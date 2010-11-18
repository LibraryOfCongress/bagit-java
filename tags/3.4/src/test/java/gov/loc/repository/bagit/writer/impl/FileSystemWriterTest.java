package gov.loc.repository.bagit.writer.impl;

import static org.junit.Assert.*;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class FileSystemWriterTest extends AbstractWriterTest {

	File bagDir;

	
	@Before
    @Override
	public void setUp() throws Exception {
		bagDir = new File(ResourceHelper.getFile("bags"), "foo");
	}

	@Override
	public File getBagFile() {
		return this.bagDir;
	}

	@Override
	public Writer getBagWriter() {
		return new FileSystemWriter(bagFactory);
	}
	
	@Test
	public void testOverwriteWithChange() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		Writer writer = this.getBagWriter();
		
		Bag newBag = writer.write(bag, this.getBagFile());
		assertTrue(newBag.verifyValid().isSuccess());

		//Add a new file on disk
		File newFile = new File(this.getBagFile(), "data/test3.txt");
		FileWriter fileWriter = new FileWriter(newFile);
		fileWriter.write("test");
		fileWriter.close();		
		assertTrue(newFile.exists());
		
		//Add a new directory on disk
		File newDir = new File(this.getBagFile(), "data/dir3");
		FileUtils.forceMkdir(newDir);
		assertTrue(newDir.exists());
		
		//Remove a file from bag
		File removeFile = new File(this.getBagFile(), "data/test1.txt");
		assertTrue(removeFile.exists());
		newBag.removeBagFile("data/test1.txt");
		assertFalse(newBag.verifyValid().isSuccess());
		
				
		//OK, now write the bag again
		Bag newBag2 = newBag.makeComplete();
		Bag newBag3 = writer.write(newBag2, this.getBagFile());
		assertTrue(newBag3.verifyValid().isSuccess());
		assertFalse(newFile.exists());
		assertFalse(newDir.exists());
		assertFalse(removeFile.exists());
	}


}
