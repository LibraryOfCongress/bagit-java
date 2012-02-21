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

public class SplitByFileTypeTest {
	BagFactory bagFactory = new BagFactory();
	SplitByFileType splitter;	
	Bag bag;
	Collection<BagFile> srcBagPayloadFiles;
	Set<String> srcBagPayloadFileDirs = new HashSet<String>();
	long srcBagPayloadSize = 0L;
	
	@Before
	public void setup() throws Exception {
		String[][] fileExtensions = new String[2][];
		fileExtensions[0] = new String[]{"txt"};
		fileExtensions[1] = new String[]{"xml", "html"};
		splitter = new SplitByFileType(this.bagFactory, fileExtensions, null);
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
	public void testSplit(){
		List<Bag> newBags = splitter.split(bag);
		try {
			boolean containsTxt = false;
			boolean containsXmlAndHtml = false;
			
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
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.txt"));
					assertTrue(bagFileDirs.contains("data/dir2/test4.txt"));
					assertTrue(bagFileDirs.contains("data/test1.txt"));
					assertTrue(bagFileDirs.contains("data/test2.txt"));
					assertEquals(bagFileDirs.size(), 5);
					containsTxt = true;
				}
				if(bagFileDirs.contains("data/dir2/dir3/test5.xml")){
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.html"));
					assertTrue(bagFileDirs.contains("data/dir1/test3.xml"));
					assertTrue(bagFileDirs.contains("data/dir1/test3.html"));
					assertTrue(bagFileDirs.contains("data/dir2/test4.xml"));
					assertTrue(bagFileDirs.contains("data/test1.xml"));
					assertEquals(bagFileDirs.size(), 6);
					containsXmlAndHtml = true;
				}
				
				
				fileSize += newBagSize;
			}
			
			assertTrue(containsXmlAndHtml);
			assertTrue(containsTxt);		
			assertEquals(fileCount, srcBagPayloadFiles.size());
			assertEquals(fileSize, this.srcBagPayloadSize);
			assertEquals(newBags.size(), 2);
		} finally {
			for(Bag bag : newBags) IOUtils.closeQuietly(bag);
		}
	}
	
	@Test
	public void testSplitExcludeDirs(){
		splitter.setExludeDirs(new String[]{"data/dir1"});
		List<Bag> newBags = splitter.split(bag);
		try {
			boolean containsTxt = false;
			boolean containsXmlAndHtml = false;
			
			int fileCount = 0;
			for(Bag newBag : newBags) {
				Collection<BagFile> bagFiles = newBag.getPayload();
				Set<String> bagFileDirs = new HashSet<String>();
				fileCount += bagFiles.size();
				for(BagFile bagFile : bagFiles) {
					bagFileDirs.add(bagFile.getFilepath());
					assertTrue(srcBagPayloadFileDirs.contains(bagFile.getFilepath()));								
				}
				
				assertFalse(bagFileDirs.contains("data/dir1/test3.txt"));
				assertFalse(bagFileDirs.contains("data/dir1/test3.xml"));
				assertFalse(bagFileDirs.contains("data/dir1/test3.html"));
				
				if(bagFileDirs.contains("data/test1.txt")){
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.txt"));
					assertTrue(bagFileDirs.contains("data/dir2/test4.txt"));
					assertTrue(bagFileDirs.contains("data/test2.txt"));
					assertEquals(bagFileDirs.size(), 4);
					containsTxt = true;
				}
				if(bagFileDirs.contains("data/dir2/dir3/test5.xml")){
					assertTrue(bagFileDirs.contains("data/dir2/dir3/test5.html"));
					assertTrue(bagFileDirs.contains("data/dir2/test4.xml"));
					assertTrue(bagFileDirs.contains("data/test1.xml"));
					assertEquals(bagFileDirs.size(), 4);
					containsXmlAndHtml = true;
				}
			}
			
			assertTrue(containsXmlAndHtml);
			assertTrue(containsTxt);		
			assertEquals(fileCount, srcBagPayloadFiles.size() - 3);
			assertEquals(newBags.size(), 2);
		} finally {
			for(Bag bag : newBags) IOUtils.closeQuietly(bag);
		}
	}
}
