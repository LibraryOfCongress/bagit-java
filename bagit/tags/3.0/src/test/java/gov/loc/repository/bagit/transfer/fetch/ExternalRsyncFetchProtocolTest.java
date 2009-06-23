package gov.loc.repository.bagit.transfer.fetch;

import static junit.framework.Assert.*;
import java.net.URI;
import gov.loc.cygwin.Cygpath;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.transfer.dest.ByteArrayFetchDestination;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class ExternalRsyncFetchProtocolTest
{
	@Test
	public void testFetchesCorrectly() throws Exception
	{
		// Note that this test sets up a non-network rsync
		// transfer, in order to avoid setting up a test
		// rsync daemon.
		
		// Store into memory, for convenience.
		ByteArrayFetchDestination destination = new ByteArrayFetchDestination("foo.txt");

		// Create a new fetcher.
		ExternalRsyncFetchProtocol protocol = new ExternalRsyncFetchProtocol();
		FileFetcher fetcher = protocol.createFetcher(new URI("rsync:///bar.txt"), null);
		
		// Get the URI to be fetched.
		String path = Cygpath.toUnix(ResourceHelper.getFile("bags/v0_96/bag/data/test1.txt").getAbsolutePath());
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
}
