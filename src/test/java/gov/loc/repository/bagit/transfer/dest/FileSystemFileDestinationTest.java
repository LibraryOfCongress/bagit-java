package gov.loc.repository.bagit.transfer.dest;

import static junit.framework.Assert.*;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class FileSystemFileDestinationTest
{
	private static File tempDir = new File("target/unittestdata/FileSystemFileDestinationTest");
	private FileSystemFileDestination unit;
	
	@Before
	public void setUp() throws Exception
	{
		if (tempDir.exists())
			FileUtils.forceDelete(tempDir);
		
		tempDir.mkdirs();
		
		this.unit = new FileSystemFileDestination(tempDir);
	}
	
	@After
	public void tearDown() throws Exception
	{
		if (tempDir.exists())
			FileUtils.forceDelete(tempDir);
	}
	
	@Test
	public void testCreatesFiles() throws Exception
	{
		FetchedFileDestination destination = this.unit.createDestination("data/foo/bar.txt", 23L);
		
		assertEquals("data/foo/bar.txt", destination.getFilepath());

		OutputStream stream = destination.openOutputStream(false);
		
		try
		{
			IOUtils.write("testing testing 1 2 3 4", stream);
		}
		finally
		{
			stream.close();
		}
		
		BagFile bagFile = destination.commit();
		
		// Make sure the bag file comes back as expected.		
		assertEquals(23L, bagFile.getSize());
		assertEquals("data/foo/bar.txt", bagFile.getFilepath());
		
		String data = IOUtils.toString(new AutoCloseInputStream(bagFile.newInputStream()));
		assertEquals("testing testing 1 2 3 4", data);
		
		// Now make sure the file is actually there.
		File resultFile = new File(tempDir, "data/foo/bar.txt");
		assertTrue(resultFile.exists());
		assertEquals(23L, resultFile.length());
		data = IOUtils.toString(new AutoCloseInputStream(new FileInputStream(resultFile)));
		assertEquals("testing testing 1 2 3 4", data);		
	}
}
