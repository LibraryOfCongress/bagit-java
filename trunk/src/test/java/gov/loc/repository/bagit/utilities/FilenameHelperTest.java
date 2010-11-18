package gov.loc.repository.bagit.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

public class FilenameHelperTest {

	@Test
	public void testRemoveBasePath() throws Exception {
		assertEquals("bar.html", FilenameHelper.removeBasePath("/foo", "/foo/bar.html"));
		assertEquals("bar.html", FilenameHelper.removeBasePath("c:/foo", "c:\\foo\\bar.html"));
		assertEquals("foo/bar.html", FilenameHelper.removeBasePath("c:\\", "c:\\foo\\bar.html"));
	}

	@Test(expected=Exception.class)
	public void testRemoveBadBasePath() throws Exception {
		FilenameHelper.removeBasePath("/xfoo", "/foo/bar.html");
	}

	@Test
	public void testNormalizePathSeparators() {
		assertEquals("data/foo.txt", FilenameHelper.normalizePathSeparators("data/foo.txt"));
		assertEquals("data/foo.txt", FilenameHelper.normalizePathSeparators("data\\foo.txt"));
	}
	
	@Test
	public void testNormalizePath() {
		assertEquals("data/foo.txt", FilenameHelper.normalizePath("data/foo.txt"));
		assertEquals("data\\foo.txt", FilenameHelper.normalizePath("data\\foo.txt"));
		assertEquals("/data/foo.txt", FilenameHelper.normalizePath("/data/foo.txt"));
		assertEquals("\\data\\foo.txt", FilenameHelper.normalizePath("\\data\\foo.txt"));
		assertEquals("data/foo.txt", FilenameHelper.normalizePath("./data/foo.txt"));
		assertEquals("data\\foo.txt", FilenameHelper.normalizePath(".\\data\\foo.txt"));
		assertEquals("data/foo.txt", FilenameHelper.normalizePath("data/./foo.txt"));
		assertEquals("data\\foo.txt", FilenameHelper.normalizePath("data\\.\\foo.txt"));
		assertEquals("foo.txt", FilenameHelper.normalizePath("data/../foo.txt"));
		assertEquals("foo.txt", FilenameHelper.normalizePath("data\\..\\foo.txt"));
		assertEquals("data/foo.txt", FilenameHelper.normalizePath("data/dir1/../foo.txt"));
		assertEquals("data\\foo.txt", FilenameHelper.normalizePath("data\\dir1\\..\\foo.txt"));

	}
	
}
