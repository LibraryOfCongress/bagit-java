package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemBagWriter;

import java.io.File;

import org.junit.Before;

public class FileSystemWriterTest extends AbstractWriterTest {

	File bagDir;

	
	@Before
	public void setUp() throws Exception {
		bagDir = new File(ResourceHelper.getFile("bags"), "foo");
	}

	@Override
	public File getBagFile() {
		return this.bagDir;
	}

	@Override
	public Writer getBagWriter() {
		return new FileSystemBagWriter(bagDir, true);
	}

}
