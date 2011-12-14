package gov.loc.repository.bagit.filesystem.impl;

import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

import org.junit.Before;

public class FileFileSystemTest extends AbstractFileSystemTest {
		
	File rootFile;

	
	@Before
	public void setup() throws Exception {
		this.rootFile =  ResourceHelper.getFile("file_systems/test");
		assert (this.rootFile.exists());
		assert (this.rootFile.isDirectory());
	}
	
	@Override
	FileSystem getFileSystem() {
		return new FileFileSystem(this.rootFile);
	}
		
}
