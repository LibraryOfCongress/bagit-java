package gov.loc.repository.bagit.filesystem.filter;

import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;

public class FalseFileSystemNodeFilter implements FileSystemNodeFilter {

	@Override
	public boolean accept(FileSystemNode fileSystemNode) {
		return false;
	}
	
}
