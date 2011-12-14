package gov.loc.repository.bagit.filesystem;

import java.io.InputStream;

public interface FileNode extends FileSystemNode {
	long getSize();
	InputStream newInputStream();
	boolean exists();
}
