package gov.loc.repository.bagit.filesystem.filter;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;

public class DirNodeFileSystemNodeFilter implements FileSystemNodeFilter {

	@Override
	public boolean accept(FileSystemNode fileSystemNode) {
		return (fileSystemNode instanceof DirNode);
	}
	
}
