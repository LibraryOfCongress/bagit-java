package gov.loc.repository.bagit.filesystem;

import java.util.Collection;

public interface DirNode extends FileSystemNode {
	Collection<FileSystemNode> listChildren();
	Collection<FileSystemNode> listChildren(FileSystemNodeFilter filter);
	FileNode childFile(String name);
	DirNode childDir(String name);
	Collection<FileSystemNode> listDescendants();
	Collection<FileSystemNode> listDescendants(FileSystemNodeFilter filter, FileSystemNodeFilter descentFilter);
	
}
