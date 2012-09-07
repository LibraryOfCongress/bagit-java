package gov.loc.repository.bagit.writer.impl;

import static org.junit.Assert.*;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.transformer.impl.UpdateCompleter;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter.WriteMode;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class FileSystemWriterTest extends AbstractWriterTest {

	File bagDir;

	private static int testCounter = 0;
	
	@Before
    @Override
	public void setUp() throws Exception {
		testCounter++;
		bagDir = new File(ResourceHelper.getFile("bags"), "writer_test" + testCounter);
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
		Bag newBag = null;
		Bag newBag2 = null;
		Bag newBag3 = null;
		
		try {
			Writer writer = this.getBagWriter();
			
			newBag = writer.write(bag, this.getBagFile());
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
			newBag2 = newBag.makeComplete();
			newBag3 = writer.write(newBag2, this.getBagFile());
			assertTrue(newBag3.verifyValid().isSuccess());
			assertFalse(newFile.exists());
			assertTrue(newDir.exists());
			assertFalse(removeFile.exists());
		} finally {
			bag.close();
			if (newBag != null) newBag.close();
			if (newBag2 != null) newBag2.close();
			if (newBag3 != null) newBag3.close();
		}
	}

	@Test
	public void testWriteTagsOnly() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		Bag newBag = null;
		Bag newBag2 = null;
		try {
			Writer writer = this.getBagWriter();
			
			newBag = writer.write(bag, this.getBagFile());
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
								
			//OK, now write the bag again
			//Bag newBag2 = newBag.makeComplete();
			((FileSystemWriter)writer).setTagFilesOnly(true);
			newBag2 = writer.write(newBag, this.getBagFile());
			assertFalse(newBag2.verifyValid().isSuccess());
			assertTrue(newFile.exists());
			assertTrue(newDir.exists());
		} finally {
			bag.close();
			if (newBag != null) newBag.close();
			if (newBag2 != null) newBag2.close();
		}
			
	}
	
	@Test
	public void testWriteFilesThatDoNotMatchManifestOnly() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		Bag newBag = null;
		Bag newBag2 = null;
		Bag newBag3 = null;
		
		try {
			Writer writer = this.getBagWriter();
			
			newBag = writer.write(bag, this.getBagFile());
			assertTrue(newBag.verifyValid().isSuccess());
			
			Long manifestLastModified = (new File(this.getBagFile(), "manifest-md5.txt")).lastModified();
			File changedFile = new File(this.getBagFile(), "data/test1.txt");
			Long changedFileLastModified = (new File(this.getBagFile(), "data/test1.txt")).lastModified();
			Long unchangedFileLastModified = (new File(this.getBagFile(), "data/test2.txt")).lastModified();
			
			Thread.sleep(1000);
			FileUtils.writeStringToFile(changedFile, "changing this file");
			assertTrue(changedFileLastModified != (new File(changedFile.getCanonicalPath())).lastModified());
			assertTrue(unchangedFileLastModified == (new File(this.getBagFile(), "data/test2.txt")).lastModified());
			
			UpdateCompleter completer = new UpdateCompleter(this.bagFactory);
			List<String> updatedFiles = new ArrayList<String>();
			updatedFiles.add("data/test1.txt");
			completer.setLimitUpdatePayloadFilepaths(updatedFiles);
			newBag2 = completer.complete(newBag);
			assertTrue(newBag2.verifyValid().isSuccess());
			
			FileSystemWriter writer2 = new FileSystemWriter(this.bagFactory);
			writer2.setFilesThatDoNotMatchManifestOnly(true);
			newBag3 = writer2.write(newBag2, this.getBagFile());
			assertTrue(newBag3.verifyValid().isSuccess());
			
			assertTrue(changedFileLastModified != (new File(this.getBagFile(), "data/test1.txt")).lastModified());
			assertTrue(unchangedFileLastModified == (new File(this.getBagFile(), "data/test2.txt")).lastModified());
			assertTrue(manifestLastModified != (new File(this.getBagFile(), "manifest-md5.txt")).lastModified());
		} finally {
			bag.close();
			if (newBag != null) newBag.close();
			if (newBag2 != null) newBag2.close();
			if (newBag3 != null) newBag3.close();
		}
		
	}

	@Test
	public void testWriteMode() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_95/bag"));
		File payloadFile = new File(bag.getFile(), "data/test1.txt");
		System.out.println("XXXXXXXXXXXX" + payloadFile);
		assertTrue(payloadFile.exists());
		Bag copyBag = null;
		Bag streamBag = null;
		Bag moveBag = null;
		
		try {
			Writer writer = this.getBagWriter();
			
			copyBag = writer.write(bag, new File(this.getBagFile().getParentFile(), "copy_bag"));
			assertTrue(copyBag.verifyValid().isSuccess());
			File newPayloadFile = new File(copyBag.getFile(), "data/test1.txt");
			assertTrue(newPayloadFile.exists());
			assertTrue(Long.valueOf(payloadFile.lastModified()).equals(newPayloadFile.lastModified()));

			((FileSystemWriter)writer).setPayloadWriteMode(WriteMode.STREAM);
			streamBag = writer.write(bag, new File(this.getBagFile().getParentFile(), "stream_bag"));
			assertTrue(streamBag.verifyValid().isSuccess());
			newPayloadFile = new File(streamBag.getFile(), "data/test1.txt");
			assertTrue(newPayloadFile.exists());
			//Since this was streamed it should have a new date
			assertFalse(Long.valueOf(payloadFile.lastModified()).equals(newPayloadFile.lastModified()));

			
			File movedPayloadFile = new File(copyBag.getFile(), "data/test1.txt");
			assertTrue(movedPayloadFile.exists());
			((FileSystemWriter)writer).setPayloadWriteMode(WriteMode.MOVE);
			moveBag = writer.write(copyBag, new File(this.getBagFile().getParentFile(), "move_bag"));
			assertTrue(moveBag.verifyValid().isSuccess());
			newPayloadFile = new File(moveBag.getFile(), "data/test1.txt");
			assertTrue(newPayloadFile.exists());
			assertTrue(Long.valueOf(payloadFile.lastModified()).equals(newPayloadFile.lastModified()));
			assertFalse(movedPayloadFile.exists());

		} finally {
			bag.close();
			if (copyBag != null) copyBag.close();
			if (streamBag != null) streamBag.close();
		}
		
	}
	
}
