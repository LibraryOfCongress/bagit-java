package gov.loc.repository.bagit.impl;

import java.io.OutputStream;

import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagInfoTxtWriter;
import gov.loc.repository.bagit.utilities.namevalue.impl.NameValueWriterImpl;

public class BagInfoTxtWriterImpl extends NameValueWriterImpl implements
		BagInfoTxtWriter {

	public BagInfoTxtWriterImpl(OutputStream out, String encoding) {
		super(out, encoding, BagInfoTxt.TYPE);
	}
	
	public BagInfoTxtWriterImpl(OutputStream out,
			String encoding, int lineLength, int indentSpaces) {
		super(out, encoding, lineLength, indentSpaces, BagInfoTxt.TYPE);
	}
}
