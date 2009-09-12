package gov.loc.repository.bagit.impl;

import java.io.InputStream;

import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.BagItTxtReader;
import gov.loc.repository.bagit.utilities.namevalue.impl.NameValueReaderImpl;

public class BagItTxtReaderImpl extends NameValueReaderImpl implements
		BagItTxtReader {

	public BagItTxtReaderImpl(String encoding, InputStream in) {
		super(encoding, in, BagItTxt.TYPE);
	}
	
}
