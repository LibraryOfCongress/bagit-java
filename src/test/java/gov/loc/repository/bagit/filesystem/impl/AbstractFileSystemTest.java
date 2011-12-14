package gov.loc.repository.bagit.filesystem.impl;

import static org.junit.Assert.*;
import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.filter.FalseFileSystemNodeFilter;
import gov.loc.repository.bagit.filesystem.filter.FileNodeFileSystemNodeFilter;

import org.junit.Test;

public abstract class AbstractFileSystemTest {

	abstract FileSystem getFileSystem();
		
	@Test
	public void testFileSystem() {
		FileSystem fs = this.getFileSystem();
		try {
			DirNode root = fs.getRoot();
			assertEquals("", root.getFilepath());
			assertNull(root.getName());

			assertEquals(5, root.listChildren().size());
			assertEquals(4, root.listChildren(new FileNodeFileSystemNodeFilter()).size());
			assertEquals(9, root.listDescendants(new FileNodeFileSystemNodeFilter(), null).size());
			assertEquals(4, root.listDescendants(new FileNodeFileSystemNodeFilter(), new FalseFileSystemNodeFilter()).size());
			assertEquals(14, root.listDescendants().size());
			
			DirNode dataDir = root.childDir("data");
			assertNotNull(dataDir);						
			assertEquals(5, dataDir.listChildren().size());						
			assertEquals("data", dataDir.getName());
			assertEquals("data", dataDir.getFilepath());
			
			assertNull(root.childDir("not a dir"));
			
			FileNode bagItTxtFile = root.childFile("bagit.txt");
			assertNotNull(bagItTxtFile);
			assertEquals("bagit.txt", bagItTxtFile.getName());
			assertEquals("bagit.txt", bagItTxtFile.getFilepath());
			
			assertNull(root.childFile("not a file"));
			
			DirNode dir1Dir = dataDir.childDir("dir1");
			assertNotNull(dir1Dir);
			assertEquals("dir1", dir1Dir.getName());
			assertEquals("data/dir1", dir1Dir.getFilepath());
			assertEquals(1, dir1Dir.listChildren().size());
			
			FileNode test1TxtFile = dataDir.childFile("test1.txt");
			assertNotNull(test1TxtFile);
			assertEquals("test1.txt", test1TxtFile.getName());
			assertEquals("data/test1.txt", test1TxtFile.getFilepath());
			
			DirNode dir4Dir = dataDir.childDir("dir4");
			assertNotNull(dir4Dir);
			assertTrue(dir4Dir.listChildren().isEmpty());
		} finally {		
			fs.closeQuietly();
		}
	}
	
}
