package gov.loc.repository.bagit.filesystem.filter;

import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;

public class OrFileSystemNodeFilter implements FileSystemNodeFilter {

	private FileSystemNodeFilter[] filters;
	
	public OrFileSystemNodeFilter(FileSystemNodeFilter... filters) {
		this.filters = filters;
	}
	
	@Override
	public boolean accept(FileSystemNode fileSystemNode) {
		for(FileSystemNodeFilter filter : this.filters) {
			if (filter.accept(fileSystemNode)) return true;
		}
		return false;
	}
	
}
