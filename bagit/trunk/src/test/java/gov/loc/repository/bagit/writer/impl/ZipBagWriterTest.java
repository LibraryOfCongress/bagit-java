package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.ZipWriter;

import java.io.File;

import org.junit.Before;

public class ZipBagWriterTest extends AbstractWriterTest {

	File bagFile;
	
	@Before
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.zip");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public Writer getBagWriter() {
		return new ZipWriter(bagFactory);

	}

}
