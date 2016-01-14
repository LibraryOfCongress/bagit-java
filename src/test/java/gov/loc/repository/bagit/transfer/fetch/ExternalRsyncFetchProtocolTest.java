package gov.loc.repository.bagit.transfer.fetch;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.transfer.dest.ByteArrayFetchDestination;
import gov.loc.repository.bagit.utilities.ResourceHelper;


public class ExternalRsyncFetchProtocolTest extends Assert
{
	private static boolean skipTests = false; 

	@BeforeClass
	public static void checkForRsyncInstallation()
	{
		ExternalRsyncFetchProtocol protocol = new ExternalRsyncFetchProtocol();

		try 
		{
			protocol.checkRsyncSanity();
			skipTests = false;
		}
		catch(BagTransferException e) 
		{
			skipTests = true;

			System.err.println("Could not find installed rysnc for unit tests.");
			e.printStackTrace(System.err);
		}
	}
	
	@Test
	public void testFetchesCorrectly() throws Exception
	{
		if (skipTests) return;
		
		// Note that this test sets up a non-network rsync
		// transfer, in order to avoid setting up a test
		// rsync daemon.
		
		// Store into memory, for convenience.
		ByteArrayFetchDestination destination = new ByteArrayFetchDestination("foo.txt");

		// Create a new fetcher.
		ExternalRsyncFetchProtocol protocol = new ExternalRsyncFetchProtocol();

		// Get the URI to be fetched.
		FileFetcher fetcher = protocol.createFetcher(new URI("rsync:///bar.txt"), null);
		String path = ResourceHelper.getFile("bags/v0_96/bag/data/test1.txt").getAbsolutePath();
		URI uri = new URI(path);
		
		// Run through the fetcher lifecycle.
		fetcher.initialize();
		fetcher.fetchFile(uri, null, destination, null);
		BagFile bagFile = destination.commit();
		fetcher.close();
		
		assertEquals("foo.txt", bagFile.getFilepath());
		String data = IOUtils.toString(bagFile.newInputStream());
		assertEquals("test1", data);
	}
	
	@Test
	public void testUnescapesSpacesBeforePassingToCommandline() throws Exception
	{
		if (skipTests) return;
		
		// Ticket #728
		// https://beryllium.rdc.lctl.gov/trac/transfer/ticket/728
		
		// Note that this test sets up a non-network rsync
		// transfer, in order to avoid setting up a test
		// rsync daemon.
		
		// Store into memory, for convenience.
		ByteArrayFetchDestination destination = new ByteArrayFetchDestination("foo bar.txt");

		// Create a new fetcher.
		ExternalRsyncFetchProtocol protocol = new ExternalRsyncFetchProtocol();
		
		// Get the URI to be fetched.
		FileFetcher fetcher = protocol.createFetcher(new URI("rsync:///bar.txt"), null);
		String path = ResourceHelper.getFile("bags/v0_96/bag-with-escapable-characters/data/test file with spaces.txt").getAbsolutePath();
		path = path.replace(" ", "%20");
		
		URI uri = new URI(path);
		
		// Run through the fetcher lifecycle.
		fetcher.initialize();
		fetcher.fetchFile(uri, null, destination, null);
		BagFile bagFile = destination.commit();
		fetcher.close();
		
		assertEquals("foo bar.txt", bagFile.getFilepath());
		String data = IOUtils.toString(bagFile.newInputStream());
		assertEquals("test file with spaces", data);
	}
}
