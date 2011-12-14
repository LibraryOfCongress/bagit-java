package gov.loc.repository.bagit.filesystem.filter;

import java.util.List;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;
import gov.loc.repository.bagit.utilities.FilenameHelper;

public class IgnoringFileSystemNodeFilter implements FileSystemNodeFilter {

	private List<String> ignoreAdditionalDirectories;
	private boolean ignoreSymlinks;
	private String relativeFilepath = null;
	
	public IgnoringFileSystemNodeFilter(List<String> ignoreAdditionalDirectories, boolean ignoreSymlinks) {
		assert ignoreAdditionalDirectories != null;
		this.ignoreAdditionalDirectories = ignoreAdditionalDirectories;
		this.ignoreSymlinks = ignoreSymlinks;
	}
	
	public void setRelativeFilepath(String relativeFilepath) {
		this.relativeFilepath = relativeFilepath;
	}
	
	@Override
	public boolean accept(FileSystemNode fileSystemNode) {
		String filepath = fileSystemNode.getFilepath();
		if (relativeFilepath != null) filepath = FilenameHelper.removeBasePath(relativeFilepath, filepath);
		if (this.ignoreSymlinks && fileSystemNode.isSymlink()) return false;
		if ((fileSystemNode instanceof DirNode) && this.ignoreAdditionalDirectories.contains(filepath)) return false;
		return true;
	}

}
