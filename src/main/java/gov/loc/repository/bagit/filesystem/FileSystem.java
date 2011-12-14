package gov.loc.repository.bagit.filesystem;

import java.io.Closeable;
import java.io.File;

public interface FileSystem extends Closeable {
	DirNode getRoot();
	/*
	 * The file that represents the file system.
	 * This may be a file or directory depending on the type of file system.
	 */
	File getFile();
	FileNode resolve(String filepath);
	void closeQuietly();
}
