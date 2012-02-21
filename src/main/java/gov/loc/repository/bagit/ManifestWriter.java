package gov.loc.repository.bagit;

import java.io.Closeable;

public interface ManifestWriter extends Closeable {
	void write(String file, String fixityValue);
}
