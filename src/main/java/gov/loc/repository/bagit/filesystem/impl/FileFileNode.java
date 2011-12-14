package gov.loc.repository.bagit.filesystem.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import gov.loc.repository.bagit.filesystem.FileNode;

public class FileFileNode extends AbstractFileNode implements FileNode {

	protected FileFileNode(File file, FileFileSystem fileSystem) {
		super(file, fileSystem);
	}

	@Override
	public long getSize() {
		return this.file.length();
	}

	@Override
	public InputStream newInputStream() {		
		try {
			return new BufferedInputStream(new FileInputStream(this.file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean exists() {
		//Exists and is a file
		return this.file.isFile();
	}

}
