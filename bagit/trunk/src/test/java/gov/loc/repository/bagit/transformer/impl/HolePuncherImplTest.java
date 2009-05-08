package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.FetchTxt;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;
import java.text.MessageFormat;


public class HolePuncherImplTest {
	
	BagFactory bagFactory = new BagFactory();
	HolePuncher puncher;

	@Before
	public void setup() {
		puncher = new HolePuncherImpl(bagFactory);
	}
	
	private Bag getBag(Bag.Format format) throws Exception {
		return this.getBag(BagFactory.LATEST, format);  
	}

	private Bag getBag(Version version, Bag.Format format) throws Exception {
		return this.bagFactory.createBag(this.getBagDir(version, format), version, true);  
	}	
	
	private File getBagDir(Version version, Bag.Format format) throws Exception {
		return ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag{1}", version.toString().toLowerCase(), format.extension));		
	}
	
	@Test
	public void testMakeHoley() throws Exception {
		Bag bag = this.getBag(Bag.Format.FILESYSTEM);
		assertEquals(5, bag.getPayload().size());
		assertNull(bag.getFetchTxt());
		
		Bag newBag = puncher.makeHoley(bag, "http://foo.com/bag", true, false);
		FetchTxt fetch = newBag.getFetchTxt();
		assertNotNull(fetch);
		assertEquals(5, fetch.size());
		FilenameSizeUrl filenameSizeUrl = fetch.get(0);
		assertEquals("data/dir2/dir3/test5.txt", filenameSizeUrl.getFilename());
		assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
		assertEquals("http://foo.com/bag/data/dir2/dir3/test5.txt", filenameSizeUrl.getUrl());
		
		assertEquals(0, newBag.getPayload().size());
		
	}
	
	@Test
	public void testMakeHoleyWithSlash() throws Exception {
		Bag bag = this.getBag(Bag.Format.FILESYSTEM);
		assertEquals(5, bag.getPayload().size());
		assertNull(bag.getFetchTxt());
				
		//Now test with a slash after the url
		bag = this.getBag(Bag.Format.FILESYSTEM);
		Bag newBag = puncher.makeHoley(bag, "http://foo.com/bag/", false, false);
		FetchTxt fetch = newBag.getFetchTxt();
		assertNotNull(fetch);
		FilenameSizeUrl filenameSizeUrl = fetch.get(0);
		assertEquals("data/dir2/dir3/test5.txt", filenameSizeUrl.getFilename());
		assertEquals(Long.valueOf(5L), filenameSizeUrl.getSize());
		assertEquals("http://foo.com/bag/dir2/dir3/test5.txt", filenameSizeUrl.getUrl());
		
	}
	
}