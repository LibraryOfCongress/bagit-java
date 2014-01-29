package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.DeclareCloseable;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystem;

import java.io.Closeable;
import java.io.InputStream;
import java.text.Normalizer;

public class FileSystemBagFile implements BagFile, DeclareCloseable {
	private Normalizer.Form[] formArray = new Normalizer.Form[] { Normalizer.Form.NFC, Normalizer.Form.NFD };
	private String filepath;
	private FileNode fileNode;
	
	public FileSystemBagFile(String filepath, FileNode fileNode) {
		this.filepath = filepath;
		this.fileNode = fileNode;
		//Normalize the fileNode
		FileSystem fileSystem = this.fileNode.getFileSystem();
		for (Normalizer.Form form : formArray) {
			FileNode newNode = fileSystem.resolve(Normalizer.normalize(this.fileNode.getFilepath(), form));
			if (newNode.exists()) {
				this.fileNode = newNode;
				break;
			}
		}
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
