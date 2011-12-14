package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.utilities.ResourceHelper;


public class HolePuncherImplTest {
	
	BagFactory bagFactory = new BagFactory();
	HolePuncher puncher;

	@Before
	public void setup() {
		puncher = new HolePuncherImpl(bagFactory);
	}
			
	@Test
	public void testMakeHoley() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_96/bag-with-space"));
		try {
			assertEquals(5, bag.getPayload().size());
			assertNull(bag.getFetchTxt());
			
			Bag newBag = puncher.makeHoley(bag, "http://foo.com/bag", true, false, false);
			try {
				FetchTxt fetch = newBag.getFetchTxt();
				assertNotNull(fetch);
				assertEquals(5, fetch.size());
				boolean foundNoSpace = false;
				boolean foundSpace = false;
				for(int i=0; i < fetch.size(); i++) {
					FilenameSizeUrl filenameSizeUrl = fetch.get(i);
					if ("data/dir2/dir3/test5.txt".equals(filenameSizeUrl.getFilename())) {
						assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
						assertEquals("http://foo.com/bag/data/dir2/dir3/test5.txt", filenameSizeUrl.getUrl());
						foundNoSpace = true;
					}
					System.out.println(filenameSizeUrl.getFilename());
					if ("data/test 1.txt".equals(filenameSizeUrl.getFilename())) {
						assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
						assertEquals("http://foo.com/bag/data/test%201.txt", filenameSizeUrl.getUrl());
						foundSpace = true;
					}
					
				}
				assertTrue(foundNoSpace);
				assertTrue(foundSpace);
				assertEquals(0, newBag.getPayload().size());
			} finally {
				bag.close();
			}
		} finally {
			bag.close();
		}
		
	}
	
	@Test
	public void testMakeHoleyWithSlash() throws Exception {
		Bag bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_96/bag"));
		try {
			assertEquals(5, bag.getPayload().size());
			assertNull(bag.getFetchTxt());
					
			//Now test with a slash after the url
			bag = this.bagFactory.createBag(ResourceHelper.getFile("bags/v0_96/bag"));
			Bag newBag = puncher.makeHoley(bag, "http://foo.com/bag/", false, false, false);
			try {
				FetchTxt fetch = newBag.getFetchTxt();
				assertNotNull(fetch);
				FilenameSizeUrl filenameSizeUrl = fetch.get(0);
				assertEquals("data/dir2/dir3/test5.txt", filenameSizeUrl.getFilename());
				assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
				assertEquals("http://foo.com/bag/dir2/dir3/test5.txt", filenameSizeUrl.getUrl());
			} finally {
				bag.close();
			}
		} finally {
			bag.close();
		}
		
	}
	
}