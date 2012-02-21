package gov.loc.repository.bagit.utilities.namevalue.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader;
import gov.loc.repository.bagit.utilities.namevalue.NameValueWriter;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader.NameValue;

public abstract class AbstractNameValueBagFile extends LinkedHashMap<String, String> implements BagFile {

	private static final long serialVersionUID = 1L;

	String filepath;
	BagFile sourceBagFile = null;
	String originalFixity = null;
	String encoding;
	
	public AbstractNameValueBagFile(String filepath, BagFile bagFile, String encoding) {
		this.filepath = filepath;
		this.sourceBagFile = bagFile;
		this.encoding = encoding;
		NameValueReader reader = new NameValueReaderImpl(encoding, sourceBagFile.newInputStream(), this.getType());
		while(reader.hasNext()) {
			NameValue nameValue = reader.next();
			this.put(nameValue.getName(), nameValue.getValue());
		}
		//Generate original fixity
		this.originalFixity = MessageDigestHelper.generateFixity(this.generatedInputStream(), Algorithm.MD5);
	}

	public AbstractNameValueBagFile(String filepath, String encoding) {
		this.filepath = filepath;
		this.encoding = encoding;
	}
	
	public String getFilepath() {
		return this.filepath;
	}

	public InputStream newInputStream() {
		//If this hasn't changed, then return sourceBagFile's inputstream
		//Otherwise, generate a new inputstream
		//This is to account for junk in the file, e.g., LF/CRs that might effect the fixity of this manifest
		if (MessageDigestHelper.fixityMatches(this.generatedInputStream(), Algorithm.MD5, this.originalFixity)) {
			return this.sourceBagFile.newInputStream();
		}
		return this.generatedInputStream();
	}

	InputStream generatedInputStream() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		NameValueWriter writer = new NameValueWriterImpl(out, this.encoding, this.getType());
		try {
			for(String name : this.keySet()) {
				writer.write(name, this.get(name));
			}
		} finally {
			IOUtils.closeQuietly(writer);
		}
		return new ByteArrayInputStream(out.toByteArray());					
	}
	
	public boolean exists() {
		return true;
	}
	
	public long getSize() {
		InputStream in = this.newInputStream();
		long size=0L;
		try {
			while(in.read() != -1) {
				size++;
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return size;
	}
	
	public abstract String getType();
	
}
