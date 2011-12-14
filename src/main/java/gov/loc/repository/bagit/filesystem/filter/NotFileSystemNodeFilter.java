package gov.loc.repository.bagit.filesystem.filter;

import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;

public class NotFileSystemNodeFilter implements FileSystemNodeFilter {

	private FileSystemNodeFilter filter;
	
	public NotFileSystemNodeFilter(FileSystemNodeFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public boolean accept(FileSystemNode fileSystemNode) {
		return ! filter.accept(fileSystemNode);
	}
	
}
