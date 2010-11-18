package gov.loc.repository.bagit.transfer.fetch;

import static junit.framework.Assert.*;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.transfer.NullFetchContext;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class LocalFileFetchProtocolTest
{
	private static File tempDir = new File("target/unittestdata/LocalFileFetchProtocolTest");
	private LocalFileFetchProtocol unit = new LocalFileFetchProtocol();
	private FileSystemFileDestination destination;

	@Before
	public void setUp() throws Exception
	{
		if (tempDir.exists())
			FileUtils.forceDelete(tempDir);
		
		tempDir.mkdirs();
		
		this.destination = new FileSystemFileDestination(tempDir);
	}

	@After
	public void tearDown() throws Exception
	{
		if (tempDir.exists())
			FileUtils.forceDelete(tempDir);
	}
	
	@Test
	public void testCopiesFiles() throws Exception
	{
		File src = ResourceHelper.getFile("bags/v0_96/bag/data/test1.txt");
		URI uri = src.toURI();

		FileFetcher fetcher = this.unit.createFetcher(uri, null);
		FetchedFileDestination destinationLocation = destination.createDestination("data/test1.txt", null);
		fetcher.fetchFile(uri, null, destinationLocation, new NullFetchContext());
		BagFile bagFile = destinationLocation.commit();
		
		// Make sure the bag file comes out correctly.
		assertEquals("data/test1.txt", bagFile.getFilepath());
		assertTrue(bagFile.exists());
		assertEquals(5L, bagFile.getSize());
		assertEquals("test1", IOUtils.toString(new AutoCloseInputStream(bagFile.newInputStream())));
		
		// And make sure the file is in the right location.
		File test1File = new File(tempDir, "data/test1.txt");
		assertTrue(test1File.exists());
		assertEquals(5L, test1File.length());
		assertEquals("test1", IOUtils.toString(new AutoCloseInputStream(new FileInputStream(test1File))));
	}
}
