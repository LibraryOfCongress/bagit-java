package gov.loc.repository.bagit.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import gov.loc.repository.bagit.BagFile;

public class StringBagFile implements BagFile {

	private String filepath;
	private byte[] buf = new byte[0];
	private static final String ENC = "utf-8";
	
	public StringBagFile(String name, byte[] data)
	{
		this.filepath = name;
		this.buf = data;
	}
	
	public StringBagFile(String name, String str) {		
		this.filepath = name;
		if (str != null) {
			try {
				this.buf = str.getBytes(ENC);
			}
			catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	@Override
	public boolean exists() {
		if (buf.length == 0) {
			return false;
		}
		return true;
	}

	@Override
	public String getFilepath() {
		return this.filepath;
	}

	@Override
	public long getSize() {
		return buf.length;
	}

	@Override
	public InputStream newInputStream() {
		return new ByteArrayInputStream(this.buf);
	}

}
