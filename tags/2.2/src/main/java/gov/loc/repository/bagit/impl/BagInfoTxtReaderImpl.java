package gov.loc.repository.bagit.impl;

import java.io.InputStream;

import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagInfoTxtReader;
import gov.loc.repository.bagit.utilities.namevalue.impl.NameValueReaderImpl;

public class BagInfoTxtReaderImpl extends NameValueReaderImpl implements
		BagInfoTxtReader {

	public BagInfoTxtReaderImpl(String encoding, InputStream in) {
		super(encoding, in, BagInfoTxt.TYPE);
	}
	
}
