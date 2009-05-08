package gov.loc.repository.bagit.impl;

import java.io.InputStream;
import java.io.OutputStream;

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
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.transformer.impl.HolePuncherImpl;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import gov.loc.repository.bagit.writer.impl.TarWriter;
import gov.loc.repository.bagit.writer.impl.ZipWriter;
import gov.loc.repository.bagit.writer.impl.TarWriter.Compression;

public abstract class AbstractBagPartFactory implements BagPartFactory {

	protected BagConstants bagConstants;
	protected BagFactory bagFactory;
	
	public AbstractBagPartFactory(BagFactory bagFactory, BagConstants bagConstants) {
		this.bagConstants = bagConstants;
		this.bagFactory = bagFactory;
		
	}
	
	@Override
	public BagItTxt createBagItTxt(BagFile bagFile) {
		return new BagItTxtImpl(bagFile, this.bagConstants);
	}

	@Override
	public BagItTxt createBagItTxt() {
		return new BagItTxtImpl(this.bagConstants);
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
	public Manifest createManifest(String name) {
		return new ManifestImpl(name, this.bagConstants, this);
	}

	@Override
	public Manifest createManifest(String name, BagFile sourceBagFile) {
		return new ManifestImpl(name, this.bagConstants, this, sourceBagFile);
	}

	@Override
	public BagInfoTxt createBagInfoTxt() {
		return new BagInfoTxtImpl(this.bagConstants);
	}
	
	@Override
	public BagInfoTxt createBagInfoTxt(BagFile bagFile) {
		return new BagInfoTxtImpl(bagFile, this.bagConstants);
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
	public FetchTxt createFetchTxt() {
		return new FetchTxtImpl(this.bagConstants, this);
	}

	@Override
	public FetchTxt createFetchTxt(BagFile sourceBagFile) {
		return new FetchTxtImpl(this.bagConstants, this, sourceBagFile);
	}

	@Override
	public FetchTxtReader createFetchTxtReader(InputStream in, String encoding) {
		return new FetchTxtReaderImpl(in, encoding);
	}

	@Override
	public FetchTxtWriter createFetchTxtWriter(OutputStream out) {
		return new FetchTxtWriterImpl(out);
	}
		
	@Override
	public Completer createCompleter() {
		return new DefaultCompleter(this.bagFactory);
	}
	
	@Override
	public HolePuncher createHolePuncher() {
		return new HolePuncherImpl(this.bagFactory);
	}

	@Override
	public Writer createWriter(Format format) {
		if (Format.FILESYSTEM.equals(format)) {
			return new FileSystemWriter(this.bagFactory);
		}
		if (Format.ZIP.equals(format)) {
			return new ZipWriter(this.bagFactory);
		}
		if (Format.TAR.equals(format)) {
			return new TarWriter(this.bagFactory);
		}
		if (Format.TAR_BZ2.equals(format)) {
			TarWriter writer = new TarWriter(this.bagFactory);
			writer.setCompression(Compression.BZ2);
			return writer;
		}
		if (Format.TAR_GZ.equals(format)) {
			TarWriter writer = new TarWriter(this.bagFactory);
			writer.setCompression(Compression.GZ);
			return writer;
		}
		throw new RuntimeException("Writing not supported for " + format);
	}
	
	@Override
	public CompleteVerifier createCompleteVerifier() {
		return new CompleteVerifierImpl();
	}
	
	@Override
	public ManifestChecksumVerifier createManifestVerifier() {
		return new ParallelManifestChecksumVerifier();
	}
	
	@Override
	public ValidVerifier createValidVerifier() {
		return this.createValidVerifier(this.createCompleteVerifier(), this.createManifestVerifier());
	}
	
	@Override
	public ValidVerifier createValidVerifier(CompleteVerifier completeVerifier,
			ManifestChecksumVerifier manifestChecksumVerifier) {
		return new ValidVerifierImpl(completeVerifier, manifestChecksumVerifier);
	}
	
}
