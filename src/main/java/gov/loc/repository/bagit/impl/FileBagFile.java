package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.BagFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileBagFile implements BagFile {
	private File file;
	private String filepath;
	
	public FileBagFile(String name, File file) {
		this.filepath = name;
		this.file = file;
		
	}
	
	public InputStream newInputStream() {
		
		try {
			return new BufferedInputStream(new FileInputStream(this.file));
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
		if (this.file != null && this.file.exists()) {
			return true;			
		}
		return false;
	}

	public long getSize() {
		if (this.exists()) {
			return this.file.length();
		}
		return 0L;
	}
}
