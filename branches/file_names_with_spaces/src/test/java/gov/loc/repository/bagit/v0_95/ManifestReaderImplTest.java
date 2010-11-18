package gov.loc.repository.bagit.v0_95;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractManifestReaderImplTest;

public class ManifestReaderImplTest extends AbstractManifestReaderImplTest {

	@Override
	public boolean canReadDoubleSpaceWithUnixSep() {
		return true;
	}

	@Override
	public boolean canReadSingleSpaceWithUnixSep() {
		return true;
	}

	@Override
	public boolean canReadSingleSpaceWithWindowsSep() {
		return true;
	}

	@Override
	public boolean canReadTabWithUnixSep() {
		return true;
	}

	@Override
	public boolean canReadTabWithUnixSepWithSpaceInFilename() {
		return true;
	}
	
	@Override
	@Test
	public void testSpaceAstericksWithUnixSep() throws Exception {
		assertTrue(this.canReadLine("8ad8757baa8564dc136c1e07507f4a98 *data/test1.txt\n", "8ad8757baa8564dc136c1e07507f4a98", "*data/test1.txt"));
		
	}
		
	@Override
	public boolean canReadSpaceAstericksWithUnixSep() {
		return true;
	}
	
	@Override
	public Version getVersion() {
		return Version.V0_95;
	}

	
}
