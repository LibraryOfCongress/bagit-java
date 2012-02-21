package gov.loc.repository.bagit.utilities.namevalue.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.namevalue.NameValueMapList;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader;
import gov.loc.repository.bagit.utilities.namevalue.NameValueWriter;
import gov.loc.repository.bagit.utilities.namevalue.NameValueReader.NameValue;

public abstract class AbstractNameValueMapListBagFile extends AbstractMap<String, String> implements BagFile, NameValueMapList {

	String filepath;
	BagFile sourceBagFile = null;
	String originalFixity = null;
	String encoding;
	
	protected List<NameValue> nameValueList = new ArrayList<NameValue>();
	
	public AbstractNameValueMapListBagFile(String filepath, BagFile bagFile, String encoding) {
		this.filepath = filepath;
		this.sourceBagFile = bagFile;
		this.encoding = encoding;
		NameValueReader reader = new NameValueReaderImpl(encoding, sourceBagFile.newInputStream(), this.getType());
		while(reader.hasNext()) {
			this.nameValueList.add(reader.next());
		}
		//Generate original fixity
		this.originalFixity = MessageDigestHelper.generateFixity(this.generatedInputStream(), Algorithm.MD5);
	}

	public AbstractNameValueMapListBagFile(String filepath, String encoding) {
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

	public InputStream generatedInputStream() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		NameValueWriter writer = new NameValueWriterImpl(out, this.encoding, this.getType());
		try {
			for(NameValue nameValue : this.nameValueList) {
				writer.write(nameValue.getName(), nameValue.getValue());
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
	
	public boolean containsKeyCaseInsensitive(String key) {
		if (this.getCaseInsensitive(key) != null) {
			return true;
		}
		return false;
	}
	
	public String getActualKey(String key) {
		for(String name : this.keySet()) {
			if (name.equalsIgnoreCase(key)) {
				return name;
			}
		}
		return null;		
	}
	
	public String getCaseInsensitive(String key) {
		if (key == null) {
			return this.get(key);
		}
		for(String name : this.keySet()) {
			if (key.equalsIgnoreCase(name)) {
				return this.get(name);
			}
		}
		return null;
	}
	
	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		List<String> keys = new ArrayList<String>();
		Set<java.util.Map.Entry<String, String>> entrySet = new HashSet<Entry<String,String>>();
		for(NameValue nameValue : this.nameValueList) {
			//Only take the first
			if (! keys.contains(nameValue.getName())) {
				entrySet.add(nameValue);
				keys.add(nameValue.getName());
			}
		}
		return entrySet;
	}
	
	@Override
	public String put(String key, String value) {
		return this.put(new NameValue(key, value));
	}

	@Override
	public String put(NameValue nameValue) {
		for(int i=0; i < this.nameValueList.size(); i++) {
			if (this.nameValueList.get(i).getName().equals(nameValue.getName())) {
				this.nameValueList.set(i, nameValue);
				return nameValue.getValue();
			}
		}
		this.nameValueList.add(nameValue);
		return nameValue.getValue();
	}
	
	@Override
	public String remove(Object key) {
		for(NameValue nameValue : this.nameValueList) {
			if (nameValue.getName().equals(key)) {
				this.nameValueList.remove(nameValue);
				return nameValue.getValue();
			}
		}
		return null;
	}
	
	@Override
	public List<String> getList(String key) {
		List<String> values = new ArrayList<String>();
		for(NameValue nameValue : this.nameValueList) {
			if (nameValue.getName().equals(key)) values.add(nameValue.getValue());
		}
		return values;
	}

	@Override
	public void putList(String key, Collection<String> values) {
		for(String value : values) {
			this.putList(key, value);
		}
	}

	@Override
	public void putListAll(Collection<NameValue> nameValues) {
		this.nameValueList.addAll(nameValues);
		
	}

	@Override
	public boolean removeList(NameValue nameValue) {
		return this.nameValueList.remove(nameValue);
	}
	
	@Override
	public boolean removeList(String key, String value) {
		return this.removeList(new NameValue(key, value));
	}

	@Override
	public Iterator<NameValue> iterator() {
		return this.nameValueList.iterator();
	}

	@Override
	public List<NameValue> asList() {
		return this.nameValueList;
	}
	
	@Override
	public int sizeList() {
		return this.nameValueList.size();
	}
	
	@Override
	public void putList(NameValue nameValue) {
		this.nameValueList.add(nameValue);
	}
	
	@Override
	public void putList(String key, String value) {
		this.nameValueList.add(new NameValue(key, value));
	}
	
	@Override
	public boolean removeAllList(String key) {
		List<NameValue> toRemove = new ArrayList<NameValue>();
		for(NameValue nameValue : this.nameValueList) {
			if (nameValue.getName().equals(key)) toRemove.add(nameValue);
		}
		return this.nameValueList.removeAll(toRemove);
	}
	
	@Override
	public void clear() {
		this.nameValueList.clear();
	}
}
