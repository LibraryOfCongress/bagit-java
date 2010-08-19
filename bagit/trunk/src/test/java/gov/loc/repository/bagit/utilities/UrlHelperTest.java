package gov.loc.repository.bagit.utilities;

import org.junit.Test;
import static org.junit.Assert.*;

public class UrlHelperTest {
	@Test
	public void test() {
		assertEquals("/foo/bar.txt", UrlHelper.encodeFilepath("/foo/bar.txt"));
		assertEquals("/foo/bar%20bar.txt", UrlHelper.encodeFilepath("/foo/bar bar.txt"));
		assertEquals("/foo/bar%2Bbar.txt", UrlHelper.encodeFilepath("/foo/bar+bar.txt"));
	}
}
