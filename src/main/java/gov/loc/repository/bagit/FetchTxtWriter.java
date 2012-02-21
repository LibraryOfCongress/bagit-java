package gov.loc.repository.bagit;

import java.io.Closeable;

public interface FetchTxtWriter extends Closeable {
	void write(String filename, Long size, String url);
}
