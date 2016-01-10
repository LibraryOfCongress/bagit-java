package gov.loc.repository.bagit;

import java.io.Closeable;

public interface FetchTxtWriter extends Closeable {
	
	/**
	 * Writes the "fetch.txt" data to an InputStream.
	 * @param filename The filename of the payload file.
	 * @param size The number of octets in the file.
	 * @param url The location of the file to be fetched.
	 * @param fetchStatus The status of the file being fetched.
	 */
	void write(String filename, Long size, String url, FetchTxt.FetchStatus fetchStatus);
	
}