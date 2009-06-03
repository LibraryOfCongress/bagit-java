package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.TarWriter;

import java.io.File;

import org.junit.Before;

public class TarWriterTest extends AbstractWriterTest {

	File bagFile;
	
	@Before
    @Override
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.tar");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public Writer getBagWriter() {
		TarWriter tarWriter = new TarWriter(bagFactory);
		return tarWriter;
	}

}
