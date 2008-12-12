package gov.loc.repository.bagit.impl;

import java.io.InputStream;
import java.io.OutputStream;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagInfoTxtReader;
import gov.loc.repository.bagit.BagInfoTxtWriter;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.BagItTxtReader;
import gov.loc.repository.bagit.BagItTxtWriter;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxtReader;
import gov.loc.repository.bagit.FetchTxtWriter;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;

public abstract class AbstractBagPartFactory implements BagPartFactory {

	@Override
	public BagItTxt createBagItTxt(BagFile bagFile) {
		return new BagItTxtImpl(bagFile, this.getBagConstants());
	}

	@Override
	public BagItTxt createBagItTxt() {
		return new BagItTxtImpl(this.getBagConstants());
	}

	@Override
	public BagItTxtReader createBagItTxtReader(String encoding, InputStream in) {
		return new BagItTxtReaderImpl(encoding, in);
	}

	@Override
	public BagItTxtWriter createBagItTxtWriter(OutputStream out,
			String encoding, int lineLength, int indentSpaces) {
		return new BagItTxtWriterImpl(out, encoding, lineLength, indentSpaces);
	}

	@Override
	public BagItTxtWriter createBagItTxtWriter(OutputStream out, String encoding) {
		return new BagItTxtWriterImpl(out, encoding);
	}

	@Override
	public Manifest createManifest(String name, Bag bag) {
		return new ManifestImpl(name, bag);
	}

	@Override
	public Manifest createManifest(String name, Bag bag, BagFile sourceBagFile) {
		return new ManifestImpl(name, bag, sourceBagFile);
	}

	@Override
	public BagInfoTxt createBagInfoTxt() {
		return new BagInfoTxtImpl(this.getBagConstants());
	}
	
	@Override
	public BagInfoTxt createBagInfoTxt(BagFile bagFile) {
		return new BagInfoTxtImpl(bagFile, this.getBagConstants());
	}
	
	@Override
	public BagInfoTxtReader createBagInfoTxtReader(String encoding,
			InputStream in) {
		return new BagInfoTxtReaderImpl(encoding, in);
	}
	
	@Override
	public BagInfoTxtWriter createBagInfoTxtWriter(OutputStream out,
			String encoding) {
		return new BagInfoTxtWriterImpl(out, encoding);
	}
	
	@Override
	public BagInfoTxtWriter createBagInfoTxtWriter(OutputStream out,
			String encoding, int lineLength, int indentSpaces) {
		return new BagInfoTxtWriterImpl(out, encoding, lineLength, indentSpaces);
	}
	
	@Override
	public FetchTxt createFetchTxt(Bag bag) {
		return new FetchTxtImpl(bag);
	}

	@Override
	public FetchTxt createFetchTxt(Bag bag, BagFile sourceBagFile) {
		return new FetchTxtImpl(bag, sourceBagFile);
	}

	@Override
	public FetchTxtReader createFetchTxtReader(InputStream in, String encoding) {
		return new FetchTxtReaderImpl(in, encoding);
	}

	@Override
	public FetchTxtWriter createFetchTxtWriter(OutputStream out) {
		return new FetchTxtWriterImpl(out);
	}
	
	protected BagConstants getBagConstants() {
		return BagFactory.getBagConstants(this.getVersion());
	}
	
}
