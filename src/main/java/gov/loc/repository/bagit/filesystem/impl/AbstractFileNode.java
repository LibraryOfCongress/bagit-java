package gov.loc.repository.bagit.filesystem.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.utilities.FilenameHelper;

public abstract class AbstractFileNode implements FileSystemNode {

	protected File file;
	protected FileFileSystem fileSystem;
	private String filepath;
	private String name;
	
	protected AbstractFileNode(File file, FileFileSystem fileSystem) {
		this.file = file;
		this.fileSystem = fileSystem;
		
		if(fileSystem.getRoot() == null) {
			this.filepath = "";
			this.name = null;
		} else {		
			//Using absolute instead of canonical so symlinks name are not dereferenced
			this.filepath = FilenameHelper.removeBasePath(fileSystem.getFile().getAbsolutePath(), file.getAbsolutePath());
			this.name = file.getName();
		}
	}
		
	public File getFile() {
		return this.file;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getFilepath() {
		return this.filepath;
	}

	@Override
	public FileSystem getFileSystem() {
		return this.fileSystem;
	}
	
	@Override
	public boolean isSymlink() {
		try {
			return FileUtils.isSymlink(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
