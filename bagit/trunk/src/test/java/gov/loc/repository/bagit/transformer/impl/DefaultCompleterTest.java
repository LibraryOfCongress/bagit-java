package gov.loc.repository.bagit.transformer.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class DefaultCompleterTest {


	Completer completer = new DefaultCompleter();
	
	@Test
	public void testComplete() throws Exception {
		Bag bag = BagFactory.createBag();
		bag.addFilesToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/dir1", BagFactory.LATEST.toString().toLowerCase())));
		bag.addFilesToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/dir2", BagFactory.LATEST.toString().toLowerCase())));
		bag.addFilesToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/test1.txt", BagFactory.LATEST.toString().toLowerCase())));
		bag.addFilesToPayload(ResourceHelper.getFile(MessageFormat.format("bags/{0}/bag/data/test2.txt", BagFactory.LATEST.toString().toLowerCase())));

		assertEquals(5, bag.getPayload().size());

		Bag newBag = completer.complete(bag);		
		assertTrue(newBag.checkComplete().isSuccess());
		assertTrue(newBag.checkValid().isSuccess());

		//Make sure that has BagIt.txt, tag manifest, payload manifest
		BagItTxt bagIt = bag.getBagItTxt();
		assertEquals("UTF-8", bagIt.getCharacterEncoding());
		assertEquals(BagFactory.LATEST.versionString, bagIt.getVersion());
		
		assertEquals(1, bag.getTagManifests().size());
		assertEquals(1, bag.getPayloadManifests().size());
		
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		assertNotNull(bagInfo);
		assertEquals("25.5", bagInfo.getPayloadOxum());
		assertEquals((new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime()), bagInfo.getBaggingDate());
		assertEquals("0.1 KB", bagInfo.getBagSize());
	}

}