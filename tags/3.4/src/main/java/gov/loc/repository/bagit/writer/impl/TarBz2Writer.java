package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.BagFactory;

public class TarBz2Writer extends TarWriter {

	public TarBz2Writer(BagFactory bagFactory) {
		super(bagFactory, Compression.BZ2);
	}
	
}
