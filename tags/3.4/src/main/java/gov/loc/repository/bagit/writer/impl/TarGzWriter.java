package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.BagFactory;

public class TarGzWriter extends TarWriter {

	public TarGzWriter(BagFactory bagFactory) {
		super(bagFactory, Compression.GZ);
	}
	
}
