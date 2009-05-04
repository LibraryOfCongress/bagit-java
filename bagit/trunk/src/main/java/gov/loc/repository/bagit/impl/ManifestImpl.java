package gov.loc.repository.bagit.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestReader;
import gov.loc.repository.bagit.ManifestWriter;
import gov.loc.repository.bagit.ManifestReader.FilenameFixity;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;

public class ManifestImpl extends LinkedHashMap<String, String> implements Manifest {
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(ManifestImpl.class);	
	
	private String name;
	private Bag bag;
	private BagFile sourceBagFile = null;
	private String originalFixity = null;
	
	public ManifestImpl(String name, Bag bag) {
		this.init(name, bag);
	}
	
	public ManifestImpl(String name, Bag bag, BagFile sourceBagFile) {
		this.init(name, bag);
		this.sourceBagFile = sourceBagFile;
		ManifestReader reader = bag.getBagPartFactory().createManifestReader(sourceBagFile.newInputStream(), bag.getBagConstants().getBagEncoding());
		while(reader.hasNext()) {
			FilenameFixity filenameFixity = reader.next();
			this.put(filenameFixity.getFilename(), filenameFixity.getFixityValue());
		}
		reader.close();
		//Generate original fixity
		this.originalFixity = MessageDigestHelper.generateFixity(this.generatedInputStream(), Algorithm.MD5);
	}
	
	private void init(String name, Bag bag) {
		log.info("Creating manifest for " + name);
		this.name = name;
		this.bag = bag;
		if (! (ManifestHelper.isPayloadManifest(name, bag.getBagConstants()) || ManifestHelper.isTagManifest(name, bag.getBagConstants()))) {
			throw new RuntimeException("Invalid name");
		}
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

	private InputStream generatedInputStream() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ManifestWriter writer = this.bag.getBagPartFactory().createManifestWriter(out);
		for(String filename : this.keySet()) {
			writer.write(filename, this.get(filename));
		}
		writer.close();
		return new ByteArrayInputStream(out.toByteArray());					
	}
	
	
	public String getFilepath() {
		return this.name;
	}

	public Algorithm getAlgorithm() {
		return ManifestHelper.getAlgorithm(this.name, this.bag.getBagConstants());
	}
	
	public boolean isPayloadManifest() {
		return ManifestHelper.isPayloadManifest(this.name, this.bag.getBagConstants());
	}
	
	public boolean isTagManifest() {
		return ManifestHelper.isTagManifest(this.name, this.bag.getBagConstants());
	}

	public SimpleResult isComplete() {
		SimpleResult result = new SimpleResult(true);
		for(String filepath : this.keySet()) {
			BagFile bagFile = null;
			if (this.isPayloadManifest()) {
				bagFile = bag.getBagFile(filepath);
			}
			else {
				bagFile = bag.getBagFile(filepath);
			}
			if (bagFile == null || ! bagFile.exists())
			{
				result.setSuccess(false);
				String message = MessageFormat.format("File {0} in manifest {1} missing from bag", filepath, this.name);
				log.info(message);
				result.addMessage(message);
			}
		}
		return result;
	}

	
	public SimpleResult isValid() {
		SimpleResult result = new SimpleResult(true);
		Algorithm algorithm = this.getAlgorithm();
		for(String filepath : this.keySet()) {
			BagFile bagFile = null;
			if (this.isPayloadManifest()) {
				bagFile = bag.getBagFile(filepath);
			}
			else {
				bagFile = bag.getBagFile(filepath);
			}
			if (bagFile != null && bagFile.exists())
			{
				if (! MessageDigestHelper.fixityMatches(bagFile.newInputStream(), algorithm, this.get(filepath))) {
					result.setSuccess(false);
					String message = MessageFormat.format("Generated fixity for file {0} does not match manifest fixity from manifest {1}", filepath, this.name);
					log.info(message);
					result.addMessage(message);
				}
			} else {
				result.setSuccess(false);
				String message = MessageFormat.format("File {0} in manifest {1} missing from bag", filepath, this.name);
				log.info(message);
				result.addMessage(message);
			}
		}
		return result;
	}

	public void generate(Collection<BagFile> bagFiles) {
		for(BagFile bagFile : bagFiles) {
			String fixity = MessageDigestHelper.generateFixity(bagFile.newInputStream(), this.getAlgorithm());
			this.put(bagFile.getFilepath(), fixity);
		}	
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
		}
		return size;
	}
	
}
