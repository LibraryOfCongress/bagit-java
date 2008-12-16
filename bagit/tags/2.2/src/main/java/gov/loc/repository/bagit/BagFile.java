package gov.loc.repository.bagit;

import java.io.InputStream;

public interface BagFile {
	
	InputStream newInputStream();
	String getFilepath();
	boolean exists();
	long getSize();
	
}
