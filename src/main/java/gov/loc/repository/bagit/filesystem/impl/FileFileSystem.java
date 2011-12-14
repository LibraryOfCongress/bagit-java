package gov.loc.repository.bagit.filesystem.impl;

import java.io.File;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystem;

public class FileFileSystem implements FileSystem {

	private File file;
	private DirNode dirNode;
	
	public FileFileSystem(File file) {
		assert file != null;		
		if (! file.isDirectory()) throw new RuntimeException("Not a directory");		
		this.file = file;
		
		this.dirNode = new FileDirNode(file, this);
	}
	
	@Override
	public void close() {
		//Do nothing
	}
	
	@Override
	public void closeQuietly() {
		//Do nothing
	}

	@Override
	public DirNode getRoot() {
		return this.dirNode;
	}

	@Override
	public File getFile() {
		return this.file;
	}
	
	@Override
	public FileNode resolve(String filepath) {
		File resolvedFile = new File(this.file, filepath);
		return new FileFileNode(resolvedFile, this);
	}

}
