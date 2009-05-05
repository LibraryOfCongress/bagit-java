package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.utilities.VFSHelper;

import java.io.InputStream;

import org.apache.commons.vfs.FileObject;

public class VFSBagFile implements BagFile {
	private FileObject fileObject;
	private String filepath;
	private String fileURI = null;
	
	public VFSBagFile(String name, FileObject fileObject) {
		this.filepath = name;
		this.fileObject = fileObject;
	}
	
	public VFSBagFile(String name, String fileURI) {
		this.filepath = name;
		this.fileURI = fileURI;
	}
	
	public FileObject getFileObject() {
		if (this.fileObject == null && this.fileURI != null) {
			this.fileObject = VFSHelper.getFileObject(this.fileURI);
		}
		return fileObject;
	}
	
	public InputStream newInputStream() {
		try
		{
			return this.getFileObject().getContent().getInputStream();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public String getFilepath() {
		return this.filepath;
	}
	
	public boolean exists() {
		try {
			if (this.getFileObject() != null && this.getFileObject().exists()) {
				return true;
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
			}
			
		return false;
	}
	
	public long getSize() {
		try {
			return this.getFileObject().getContent().getSize();
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
