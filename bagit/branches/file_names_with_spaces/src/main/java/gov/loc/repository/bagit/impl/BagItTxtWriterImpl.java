package gov.loc.repository.bagit.impl;

import java.io.OutputStream;

import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.BagItTxtWriter;
import gov.loc.repository.bagit.utilities.namevalue.impl.NameValueWriterImpl;

public class BagItTxtWriterImpl extends NameValueWriterImpl implements
		BagItTxtWriter {

	public BagItTxtWriterImpl(OutputStream out, String encoding) {
		super(out, encoding, BagItTxt.TYPE);
	}
	
	public BagItTxtWriterImpl(OutputStream out,
			String encoding, int lineLength, int indentSpaces) {
		super(out, encoding, lineLength, indentSpaces, BagItTxt.TYPE);
	}
}
