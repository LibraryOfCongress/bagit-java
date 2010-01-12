package gov.loc.repository.bagit.v0_96;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.ManifestReader;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.ManifestReader.FilenameFixity;
import gov.loc.repository.bagit.impl.AbstractManifestReaderImplTest;
import gov.loc.repository.bagit.utilities.OperatingSystemHelper;

import org.junit.Test;


public class ManifestReaderImplTest extends AbstractManifestReaderImplTest{

	BagFactory bagFactory = new BagFactory();
	
	@Test
	public void test() throws Exception {
		BagPartFactory factory = this.bagFactory.getBagPartFactory(Version.V0_96);
		String manifest = 
			"8ad8757baa8564dc136c1e07507f4a98 data/test1.txt\n" +
			"8ad8757baa8564dc136c1e07507f4a98	data/test3.txt\n" +
			"8ad8757baa8564dc136c1e07507f4a98	data/test 4.txt\n" +
			"8ad8757baa8564dc136c1e07507f4a98  data/test5.txt\n" +
			"8ad8757baa8564dc136c1e07507f4a98 *data/test6.txt\n" +
			"8ad8757baa8564dc136c1e07507f4a98__data/test7.txt\n";
		
		ManifestReader reader = factory.createManifestReader(new ByteArrayInputStream(manifest.getBytes("utf-8")), "utf-8");
		FilenameFixity ff = reader.next();
		assertEquals("8ad8757baa8564dc136c1e07507f4a98", ff.getFixityValue());
		assertEquals("data/test1.txt", ff.getFilename());
				
		ff = reader.next();
		assertEquals("8ad8757baa8564dc136c1e07507f4a98", ff.getFixityValue());
		assertEquals("data/test3.txt", ff.getFilename());
		
		ff = reader.next();
		assertEquals("8ad8757baa8564dc136c1e07507f4a98", ff.getFixityValue());
		assertEquals("data/test 4.txt", ff.getFilename());
		
		ff = reader.next();
		assertEquals("8ad8757baa8564dc136c1e07507f4a98", ff.getFixityValue());
		assertEquals("data/test5.txt", ff.getFilename());

		ff = reader.next();
		assertEquals("8ad8757baa8564dc136c1e07507f4a98", ff.getFixityValue());
		assertEquals("data/test6.txt", ff.getFilename());
		
		//Skip bad lines
		assertFalse(reader.hasNext());
		
		reader.close();

		
	}
	
	@Override
	public boolean canReadDoubleSpaceWithUnixSep() {
		return true;
	}

	@Override
	public boolean canReadSingleSpaceWithUnixSep() {
		return true;
	}
		
	@Override
	public void testSingleSpaceWithWindowsSep() throws Exception {
		//Reading \ as a file char, not a path separator
		boolean isException = false;
		try {
			assertEquals(true, this.canReadLine("8ad8757baa8564dc136c1e07507f4a98 data\\test1.txt\n", "8ad8757baa8564dc136c1e07507f4a98", "data\\test1.txt"));
		} catch(RuntimeException ex) {
			isException = false;
		}
		if (OperatingSystemHelper.isWindows()) {
			assertTrue(isException);
		} else {
			assertFalse(isException);
		}

		//Reading / as a path separator
		assertEquals(true, this.canReadLine("8ad8757baa8564dc136c1e07507f4a98 data\\test1.txt\n", "8ad8757baa8564dc136c1e07507f4a98", "data/test1.txt", true));		
	}
	
	@Override
	public boolean canReadSpaceAstericksWithUnixSep() {
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
	public Version getVersion() {
		return Version.V0_96;
	}

	@Test
	public void testFoo() throws Exception {
		System.out.println("XXXXXXXX");
		String f = new File("REMOVEME", "data/foo.txt").getCanonicalPath();
		f = f.substring(f.indexOf("REMOVEME") + 9);
		System.out.println(f);
		System.out.println(new File("REMOVEME", "data/foo.txt").getCanonicalPath());
		System.out.println(new File("./data/foo.txt").getCanonicalPath());
		System.out.println(new File("data/./foo.txt").getCanonicalPath());
		System.out.println(new File("data/../foo.txt").getCanonicalPath());
	}
	
}
