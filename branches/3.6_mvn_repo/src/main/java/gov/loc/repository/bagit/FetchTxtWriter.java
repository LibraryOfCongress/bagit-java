package gov.loc.repository.bagit;

public interface FetchTxtWriter {
	void close();
	void write(String filename, Long size, String url);
}
