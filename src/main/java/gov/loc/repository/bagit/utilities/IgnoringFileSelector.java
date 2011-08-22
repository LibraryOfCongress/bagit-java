package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.apache.commons.vfs.provider.local.LocalFileName;

public class IgnoringFileSelector implements FileSelector {

	private List<String> ignoreAdditionalDirectories;
	private boolean ignoreSymlinks;
	
	public IgnoringFileSelector(List<String> ignoreAdditionalDirectories, boolean ignoreSymlinks) {
		assert ignoreAdditionalDirectories != null;
		this.ignoreAdditionalDirectories = ignoreAdditionalDirectories;
		this.ignoreSymlinks = ignoreSymlinks;
	}
	
	@Override
	public boolean includeFile(FileSelectInfo info) throws Exception {
		if(info.getFile().getType().equals(FileType.FILE) && (! ignoreSymlinks || ! isSymlink(info.getFile()))) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean traverseDescendents(FileSelectInfo info)
			throws Exception {
		if((ignoreSymlinks && isSymlink(info.getFile())) || ( info.getFile().getType().equals(FileType.FOLDER) && ignoreAdditionalDirectories.contains(info.getFile().getName().getBaseName()))) {
			return false;
		}
		return true;
	}

	public boolean isSymlink(FileObject fileObj) {
		if (! (fileObj instanceof LocalFile)) return false;
		LocalFile localFile = (LocalFile)fileObj;
		try {
	        String rootFile = ((LocalFileName)localFile.getFileSystem().getRootName()).getRootFile();
	        String fileName = rootFile + localFile.getName().getPathDecoded();
			File file = new File(fileName);
			return FileUtils.isSymlink(file);
		} catch (Exception e) {
			throw new RuntimeException("Error checking if symlink", e);
		}

	}
	
}
