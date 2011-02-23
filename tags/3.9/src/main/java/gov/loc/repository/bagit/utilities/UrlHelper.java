package gov.loc.repository.bagit.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlHelper {
	public static String encodeFilepath(String s) {
		try {
			return URLEncoder.encode(s, "utf-8").replace("%2F", "/").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
