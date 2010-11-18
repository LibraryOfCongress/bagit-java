package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;

import java.io.File;

import org.junit.Before;

public class TarBz2WriterTest extends AbstractWriterTest {

	File bagFile;
	
	@Before
    @Override
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.tar.gz");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public Writer getBagWriter() {
		return new TarBz2Writer(bagFactory);
	}

}
