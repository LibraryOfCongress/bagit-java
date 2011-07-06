package gov.loc.repository.bagit.utilities;

import java.util.List;

import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;

public class IgnoringFileSelector implements FileSelector {

	private List<String> ignoreAdditionalDirectories;
	
	public IgnoringFileSelector(List<String> ignoreAdditionalDirectories) {
		assert ignoreAdditionalDirectories != null;
		this.ignoreAdditionalDirectories = ignoreAdditionalDirectories;
	}
	
	@Override
	public boolean includeFile(FileSelectInfo info) throws Exception {
		if(info.getFile().getType().equals(FileType.FILE)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean traverseDescendents(FileSelectInfo info)
			throws Exception {
		if(info.getFile().getType().equals(FileType.FOLDER) && ignoreAdditionalDirectories.contains(info.getFile().getName().getBaseName())) {
			return false;
		}
		return true;
	}
	
}
