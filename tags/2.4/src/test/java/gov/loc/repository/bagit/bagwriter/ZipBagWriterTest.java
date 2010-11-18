package gov.loc.repository.bagit.bagwriter;

import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.bagwriter.ZipBagWriter;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

import org.junit.Before;

public class ZipBagWriterTest extends AbstractBagWriterTest {

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
	public BagWriter getBagWriter() {
		return new ZipBagWriter(bagFile);
	}

}
