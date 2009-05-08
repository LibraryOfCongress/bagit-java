package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.TarWriter;
import gov.loc.repository.bagit.writer.impl.TarWriter.Compression;

import java.io.File;

import org.junit.Before;

public class TarBz2WriterTest extends AbstractWriterTest {

	File bagFile;
	
	@Before
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.tar.gz");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public Writer getBagWriter() {
		TarWriter tarWriter = new TarWriter(bagFactory);
		tarWriter.setCompression(Compression.GZ);
		return tarWriter;
	}

}
