package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestHelper;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.bag.CancelTriggeringBagDecorator;
import gov.loc.repository.bagit.progresslistener.LoggingProgressListener;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DefaultCompleterTest {

	BagFactory bagFactory = new BagFactory();
	DefaultCompleter completer;	
	Bag bag;
	
	@Before
	public void setup() throws Exception {
		completer = new DefaultCompleter(this.bagFactory);
		completer.addProgressListener(new LoggingProgressListener());
		bag = bagFactory.createBag();
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/dir1", BagFactory.LATEST.toString().toLowerCase())));
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/dir2", BagFactory.LATEST.toString().toLowerCase())));
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/test1.txt", BagFactory.LATEST.toString().toLowerCase())));
		bag.addFileToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/test2.txt", BagFactory.LATEST.toString().toLowerCase())));
		Manifest manifest = bag.getBagPartFactory().createManifest(ManifestHelper.getPayloadManifestFilename(Algorithm.SHA1, bag.getBagConstants()));
		manifest.put("data/test1.txt", "b444ac06613fc8d63795be9ad0beaf55011936ac");
		bag.putBagFile(manifest); 
		bag.putBagFile(bag.getBagPartFactory().createManifest(ManifestHelper.getTagManifestFilename(Algorithm.SHA1, bag.getBagConstants()))); 
		assertEquals(5, bag.getPayload().size());
		assertNotNull(bag.getPayloadManifest(Algorithm.SHA1));
		assertNotNull(bag.getTagManifest(Algorithm.SHA1));
		assertNotNull(bag.getChecksums("data/test1.txt").get(Algorithm.SHA1));
	}

	@After
	public void cleanup() {
		IOUtils.closeQuietly(bag);
	}
	
	@Test
	public void testComplete() throws Exception {
		Bag newBag = completer.complete(bag);
		try {
			assertTrue(newBag.verifyComplete().isSuccess());
			assertTrue(newBag.verifyValid().isSuccess());
			assertNotNull(newBag.getPayloadManifest(Algorithm.SHA1));
			assertNull(newBag.getTagManifest(Algorithm.SHA1));
			assertNotNull(newBag.getChecksums("data/test1.txt").get(Algorithm.SHA1));
			assertNull(newBag.getChecksums("data/test1.txt").get(Algorithm.MD5));
			
			//Make sure that has BagIt.txt, tag manifest, payload manifest
			BagItTxt bagIt = newBag.getBagItTxt();
			assertEquals("UTF-8", bagIt.getCharacterEncoding());
			assertEquals(BagFactory.LATEST.versionString, bagIt.getVersion());
			
			assertEquals(1, newBag.getTagManifests().size());
			assertEquals(2, newBag.getPayloadManifests().size());
			
			BagInfoTxt bagInfo = newBag.getBagInfoTxt();
			assertNotNull(bagInfo);
			assertEquals("25.5", bagInfo.getPayloadOxum());
			assertEquals((new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime()), bagInfo.getBaggingDate());
			assertEquals("0.2 KB", bagInfo.getBagSize());
		} finally {
			newBag.close();
		}
	}
	
	@Test
	public void testCompleteWithClear() throws Exception {
		completer.setClearExistingPayloadManifests(true);
		Bag newBag = completer.complete(bag);
		try {
			assertTrue(newBag.verifyComplete().isSuccess());
			assertTrue(newBag.verifyValid().isSuccess());
			assertNull(newBag.getPayloadManifest(Algorithm.SHA1));
			assertNull(newBag.getTagManifest(Algorithm.SHA1));
			assertNull(newBag.getChecksums("data/test1.txt").get(Algorithm.SHA1));
			assertNotNull(newBag.getChecksums("data/test1.txt").get(Algorithm.MD5));
			
			//Make sure that has BagIt.txt, tag manifest, payload manifest
			BagItTxt bagIt = newBag.getBagItTxt();
			assertEquals("UTF-8", bagIt.getCharacterEncoding());
			assertEquals(BagFactory.LATEST.versionString, bagIt.getVersion());
			
			assertEquals(1, newBag.getTagManifests().size());
			assertEquals(1, newBag.getPayloadManifests().size());
			
			BagInfoTxt bagInfo = newBag.getBagInfoTxt();
			assertNotNull(bagInfo);
			assertEquals("25.5", bagInfo.getPayloadOxum());
			assertEquals((new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime()), bagInfo.getBaggingDate());
			assertEquals("0.2 KB", bagInfo.getBagSize());
		} finally {
			newBag.close();
		}
	}

	@Test
	public void testCancel() throws Exception {		
		assertNull(completer.complete(new CancelTriggeringBagDecorator(bag, 3, completer)));	
	}
}