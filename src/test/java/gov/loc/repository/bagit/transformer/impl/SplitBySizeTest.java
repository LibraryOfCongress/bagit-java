package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SplitBySizeTest {
	BagFactory bagFactory = new BagFactory();
	SplitBySize splitter;	
	Bag bag;
	Collection<BagFile> srcBagPayloadFiles;
	Set<String> srcBagPayloadFileDirs = new HashSet<String>();
	Double maxPayloadSize;
	long srcBagPayloadSize = 0L;
	
	@Before
	public void setup() throws Exception {
		maxPayloadSize = new Double(20);
		splitter = new SplitBySize(this.bagFactory, maxPayloadSize, false, null);
		File sourceBagDir = ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag-split", Version.V0_96.toString().toLowerCase()));
		bag = bagFactory.createBag(sourceBagDir, BagFactory.LoadOption.BY_FILES);
		srcBagPayloadFiles = bag.getPayload();
		for(BagFile bagFile : srcBagPayloadFiles){
			srcBagPayloadFileDirs.add(bagFile.getFilepath());
			srcBagPayloadSize += bagFile.getSize();
		}
		assertEquals(srcBagPayloadFileDirs.size(), 11);
		assertEquals(srcBagPayloadSize, 55);
	}

	@After
	public void cleanup() {
		IOUtils.closeQuietly(bag);
	}
	
	@Test
	public void testSplit() {
		List<Bag> newBags = splitter.split(bag);
		try {
			int fileCount = 0;
			long fileSize = 0L;
			for(Bag newBag : newBags) {
				long newBagSize = 0L;
				Collection<BagFile> bagFiles = newBag.getPayload();
				fileCount += bagFiles.size();
				for(BagFile bagFile : bagFiles) {
					newBagSize += bagFile.getSize();				
					assertTrue(srcBagPayloadFileDirs.contains(bagFile.getFilepath()));
				}
				assertTrue(newBagSize <= this.maxPayloadSize);
				fileSize += newBagSize;
			}
			
			assertEquals(fileCount, srcBagPayloadFiles.size());
			assertEquals(fileSize, this.srcBagPayloadSize);
			assertEquals(newBags.size(), 3);
		} finally {
			for(Bag bag : newBags) IOUtils.closeQuietly(bag);
		}
	}

	@Test
	public void testSplitWithoutBagInfoTxt() {
		bag.removeBagFile(bag.getBagConstants().getBagInfoTxt());
		List<Bag> newBags = splitter.split(bag);
		try {
			int fileCount = 0;
			long fileSize = 0L;
			for(Bag newBag : newBags) {
				long newBagSize = 0L;
				Collection<BagFile> bagFiles = newBag.getPayload();
				fileCount += bagFiles.size();
				for(BagFile bagFile : bagFiles) {
					newBagSize += bagFile.getSize();				
					assertTrue(srcBagPayloadFileDirs.contains(bagFile.getFilepath()));
				}
				assertTrue(newBagSize <= this.maxPayloadSize);
				fileSize += newBagSize;
			}
			
			assertEquals(fileCount, srcBagPayloadFiles.size());
			assertEquals(fileSize, this.srcBagPayloadSize);
			assertEquals(newBags.size(), 3);
		} finally {
			for(Bag bag : newBags) IOUtils.closeQuietly(bag);
		}
	}

	
	@Test
	public void testSplitKeepLowestLevelDir(){
		splitter.setKeepLowestLevelDir(true);
		assertTrue(splitter.isKeepLowestLevelDir());
		List<Bag> newBags = splitter.split(bag);
		try {
			boolean containsDir1 = false;
			boolean containsDir3 = false;
			
			int fileCount = 0;
			long fileSize = 0L;
			for(Bag newBag : newBags) {
				long newBagSize = 0L;
				Collection<BagFile> bagFiles = newBag.getPayload();
				Set<String> bagFileDirs = new HashSet<String>();
				fileCount += bagFiles.size();
				for(BagFile bagFile : bagFiles) {
					newBagSize += bagFile.getSize();		
					bagFileDirs.add(bagFile.getFilepath());
					assertTrue(srcBagPayloadFileDirs.contains(bagFile.getFilepath()));								
				}
				
				if(bagFileDirs.contains("data/dir1/test3.txt")){
					assertTrue(bagFileDirs.contains("data/dir1/test3.xml"));
					assertTrue(bagFileDirs.contains("data/dir1/test3.html"));
					containsDir1 = true;
				}
				if(bagFileDirs.contains("data/dir2/dir3/test5.txt")){
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.xml"));
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.html"));
					containsDir3 = true;
				}
				
				
				assertTrue(newBagSize <= this.maxPayloadSize);
				fileSize += newBagSize;
			}
			
			assertTrue(containsDir1);
			assertTrue(containsDir3);		
			assertEquals(fileCount, srcBagPayloadFiles.size());
			assertEquals(fileSize, this.srcBagPayloadSize);
			assertEquals(newBags.size(), 3);
		} finally {
			for(Bag bag : newBags) IOUtils.closeQuietly(bag);
;
		}
			
	}
	
	@Test
	public void testSplitExcludeDirs(){
		splitter.setExludeDirs(new String[]{"data/dir1"});
		assertEquals(splitter.getExludeDirs().length, 1);
		List<Bag> newBags = splitter.split(bag);
		
		int fileCount = 0;
		for(Bag newBag : newBags) {
			Collection<BagFile> bagFiles = newBag.getPayload();
			Set<String> bagFileDirs = new HashSet<String>();
			fileCount += bagFiles.size();
			for(BagFile bagFile : bagFiles) {
				bagFileDirs.add(bagFile.getFilepath());
			}
			
			assertFalse(bagFileDirs.contains("data/dir1/test3.txt"));
			assertFalse(bagFileDirs.contains("data/dir1/test3.xml"));
			assertFalse(bagFileDirs.contains("data/dir1/test3.html"));
		}

		assertEquals(fileCount, srcBagPayloadFiles.size() - 3);
		assertEquals(newBags.size(), 2);
	}
	
	@Test
	public void testSplitKeepLowestLevelDirAndExcludeDirs(){
		splitter.setKeepLowestLevelDir(true);
		splitter.setExludeDirs(new String[]{"data/dir1"});
		List<Bag> newBags = splitter.split(bag);
		try {
			boolean containsDir3 = false;
			
			int fileCount = 0;
			for(Bag newBag : newBags) {
				long newBagSize = 0L;
				Collection<BagFile> bagFiles = newBag.getPayload();
				Set<String> bagFileDirs = new HashSet<String>();
				fileCount += bagFiles.size();
				for(BagFile bagFile : bagFiles) {
					newBagSize += bagFile.getSize();		
					bagFileDirs.add(bagFile.getFilepath());
					assertTrue(srcBagPayloadFileDirs.contains(bagFile.getFilepath()));								
				}
				
				assertFalse(bagFileDirs.contains("data/dir1/test3.txt"));
				assertFalse(bagFileDirs.contains("data/dir1/test3.xml"));
				assertFalse(bagFileDirs.contains("data/dir1/test3.html"));
				
				if(bagFileDirs.contains("data/dir2/dir3/test5.txt")){
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.xml"));
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.html"));
					containsDir3 = true;
				}
							
				assertTrue(newBagSize <= this.maxPayloadSize);
			}
			
			assertTrue(containsDir3);		
			assertEquals(fileCount, srcBagPayloadFiles.size() - 3);
			assertEquals(newBags.size(), 2);
		} finally {
			for(Bag bag : newBags) IOUtils.closeQuietly(bag);
		}
			
	}
	
	@Test
	public void testSplitWithException(){
		this.maxPayloadSize = new Double(10);
		splitter.setKeepLowestLevelDir(true);
		splitter.setMaxBagSize(this.maxPayloadSize);
		splitter.setExludeDirs(null);
		boolean caughtEx = false;
		try{
			splitter.split(bag);			
		}catch(Exception e){
			caughtEx = true;
		}
		assertTrue(caughtEx);
		
		this.maxPayloadSize = new Double(4);
		splitter.setMaxBagSize(this.maxPayloadSize);
		caughtEx = false;
		try{
			splitter.split(bag);			
		}catch(Exception e){
			caughtEx = true;
		}
		assertTrue(caughtEx);
	}
}
