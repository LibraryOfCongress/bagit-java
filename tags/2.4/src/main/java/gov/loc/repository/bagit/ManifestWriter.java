package gov.loc.repository.bagit;

public interface ManifestWriter {
	void close();
	void write(String file, String fixityValue);
}
