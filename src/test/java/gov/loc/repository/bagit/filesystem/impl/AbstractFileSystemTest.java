package gov.loc.repository.bagit.filesystem.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.filter.FalseFileSystemNodeFilter;
import gov.loc.repository.bagit.filesystem.filter.FileNodeFileSystemNodeFilter;

import org.apache.commons.io.IOUtils;
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
	
	@Test
	public void testMultithreading() throws Exception {
		
		FileSystem fs = this.getFileSystem();
		try {
			TestUncaughtExceptionHandler exHandler = new TestUncaughtExceptionHandler();
			List<Thread> threads = new ArrayList<Thread>();
			for(int i=0; i < 10; i++) {
				Thread t = new Thread(new TestRunnable(fs, 5));
				threads.add(t);
				t.setUncaughtExceptionHandler(exHandler);
				t.start();
			}
			
			for(Thread t : threads) {
				t.join();
			}
			assertFalse(exHandler.exceptionCaught);
			
		} finally {
			fs.closeQuietly();
		}
	}
	
	private class TestUncaughtExceptionHandler implements UncaughtExceptionHandler {

		public boolean exceptionCaught = false;
		
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			exceptionCaught = true;
		}
		
	}
	
	private class TestRunnable implements Runnable {

		private FileSystem fileSystem;
		private int iterations;
		
		public TestRunnable(FileSystem fileSystem, int iterations) {
			this.fileSystem = fileSystem;
			this.iterations = iterations;
		}
		
		@Override
		public void run() {
			for(int i=0; i < iterations; i++) {
				Collection<FileSystemNode> nodes = fileSystem.getRoot().listDescendants(new FileNodeFileSystemNodeFilter(), null);
				for(FileSystemNode node : nodes) {
					FileNode fileNode = (FileNode)node;
					InputStream in = fileNode.newInputStream();
					try {
						IOUtils.toByteArray(in);
					} catch(IOException e) {
						throw new RuntimeException();
					} finally {
						IOUtils.closeQuietly(in);
					}
				}
				
			}
		}
		
	}
	
}
