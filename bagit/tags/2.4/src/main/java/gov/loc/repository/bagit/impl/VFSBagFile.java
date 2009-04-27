package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.BagFile;

import java.io.InputStream;

import org.apache.commons.vfs.FileObject;

public class VFSBagFile implements BagFile {
	private FileObject fileObject;
	private String filepath;
	
	public VFSBagFile(String name, FileObject fileObject) {
		this.filepath = name;
		this.fileObject = fileObject;
		
	}
	
	public InputStream newInputStream() {
		try
		{
			return this.fileObject.getContent().getInputStream();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public String getFilepath() {
		return this.filepath;
	}

	public FileObject getFileObject() {
		return this.fileObject;
	}
	
	public boolean exists() {
		try {
			if (this.fileObject != null && this.fileObject.exists()) {
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
			return this.fileObject.getContent().getSize();
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
