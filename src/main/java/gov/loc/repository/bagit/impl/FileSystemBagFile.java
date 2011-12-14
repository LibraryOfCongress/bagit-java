package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.DeclareCloseable;
import gov.loc.repository.bagit.filesystem.FileNode;

import java.io.Closeable;
import java.io.InputStream;

public class FileSystemBagFile implements BagFile, DeclareCloseable {
	private String filepath;
	private FileNode fileNode;
	
	public FileSystemBagFile(String filepath, FileNode fileNode) {
		this.filepath = filepath;
		this.fileNode = fileNode;
	}
	
	public FileNode getFileNode() {
		return this.fileNode;
	}
		
	public InputStream newInputStream() {
		return this.fileNode.newInputStream();
	}
	
	public String getFilepath() {
		return this.filepath;
	}
	
	public boolean exists() {
		return this.fileNode.exists();
	}
	
	public long getSize() {
		return this.fileNode.getSize();
	}
	
	@Override
	public Closeable declareCloseable() {
		return this.fileNode.getFileSystem();
	}
}
