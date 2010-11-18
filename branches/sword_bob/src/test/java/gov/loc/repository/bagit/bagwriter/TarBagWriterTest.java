package gov.loc.repository.bagit.bagwriter;

import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.bagwriter.TarBagWriter;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

import org.junit.Before;

public class TarBagWriterTest extends AbstractBagWriterTest {

	File bagFile;
	
	@Before
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.tar");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public BagWriter getBagWriter() {
		return new TarBagWriter(bagFile);
	}

}
