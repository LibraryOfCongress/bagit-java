package gov.loc.repository.bagit.filesystem;

public interface FileSystemNode {
	String getName();
	String getFilepath();
	FileSystem getFileSystem();
	boolean isSymlink();
}
