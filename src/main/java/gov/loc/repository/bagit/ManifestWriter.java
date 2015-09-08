package gov.loc.repository.bagit;

import java.io.Closeable;

public interface ManifestWriter extends Closeable {
	
	/**
	 * Writes the FilenameFixity values to an InputStream.
	 * @param file Name of the file.
	 * @param fixityValue A checksum algorithm hash function value.
	 */
	void write(String file, String fixityValue);
}
