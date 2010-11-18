package gov.loc.repository.bagit.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

public class SizeHelperTest {

	@Test
	public void testGetSize() {
		assertEquals("0.01 KB", SizeHelper.getSize(10L));
		assertEquals("0.1 KB", SizeHelper.getSize(100L));
		assertEquals("1 KB", SizeHelper.getSize(1024L));
		assertEquals("1.5 KB",SizeHelper.getSize(1500L));
		assertEquals("1 MB", SizeHelper.getSize(1048576L));
		assertEquals("1 MB", SizeHelper.getSize(1048577L));
		assertEquals("1.1 MB", SizeHelper.getSize(1148576L));
		assertEquals("1 GB", SizeHelper.getSize(1073741824L));
		assertEquals("1 TB", SizeHelper.getSize(1099511627776L));
		
	}

}
