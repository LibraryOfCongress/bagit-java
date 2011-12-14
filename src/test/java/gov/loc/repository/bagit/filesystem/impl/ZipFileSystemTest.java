package gov.loc.repository.bagit.filesystem.impl;

import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

import org.junit.Before;

public class ZipFileSystemTest extends AbstractFileSystemTest {
		
	File rootFile;

	
	@Before
	public void setup() throws Exception {
		this.rootFile =  ResourceHelper.getFile("file_systems/test.zip");
		assert (this.rootFile.exists());
		assert (this.rootFile.isFile());
	}
	
	@Override
	FileSystem getFileSystem() {
		return new ZipFileSystem(this.rootFile);
	}
		
}
