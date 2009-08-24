package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.format;
import static junit.framework.Assert.*;
import java.net.URI;
import gov.loc.cygwin.Cygpath;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.transfer.dest.ByteArrayFetchDestination;
import gov.loc.repository.bagit.utilities.ResourceHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class ExternalRsyncFetchProtocolTest
{
	//private boolean rsyncProtocolExists = true;
	private static final Log log = LogFactory.getLog(ExternalRsyncFetchProtocol.class);
	
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
		//Doing sanity check to make sure rsync installed on this machine
		try {
			protocol.checkRsyncSanity();
		} catch(BagTransferException bte) {
			//rsyncProtocolExists = false;
			log.warn(format("rsync is not installed on this machine."), bte);
			return;
		}
		
		// Get the URI to be fetched.
		FileFetcher fetcher = protocol.createFetcher(new URI("rsync:///bar.txt"), null);
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
