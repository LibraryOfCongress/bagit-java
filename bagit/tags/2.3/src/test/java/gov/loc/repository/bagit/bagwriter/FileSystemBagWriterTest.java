package gov.loc.repository.bagit.bagwriter;

import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.bagwriter.FileSystemBagWriter;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

import org.junit.Before;

public class FileSystemBagWriterTest extends AbstractBagWriterTest {

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
	public BagWriter getBagWriter() {
		return new FileSystemBagWriter(bagDir, true);
	}

}
