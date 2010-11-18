package gov.loc.repository.bagit.v0_95;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.StringBagFile;

import org.junit.Test;


public class BagInfoTxtImplTest {

	BagPartFactory factory = BagFactory.getBagPartFactory(Version.V0_95);
	BagConstants constants = BagFactory.getBagConstants(Version.V0_95);
	
	@Test
	public void testBagInfoTxt() {
		String bagInfoTxtStr = 
			"Source-Organization: Spengler University\n" +
			"Organization-Address: 1400 Elm St., Cupertino, California, 95014\n" +
			"Contact-Name: Edna Janssen\n" +
			"Contact-Phone: +1 408-555-1212\n" +
			"Contact-Email: ej@spengler.edu\n" +
			"External-Description: Uncompressed greyscale TIFF images from the\n" +
			"     Yoshimuri papers collection.\n" +
			"Packing-Date: 2008-01-15\n" +
			"External-Identifier: spengler_yoshimuri_001\n" +
			"Package-Size: 260 GB\n" +
			"Bag-Group-Identifier: spengler_yoshimuri\n" +
			"Bag-Count: 1 of 15\n" +
			"Internal-Sender-Identifier: /storage/images/yoshimuri\n" +
			"Internal-Sender-Description: Uncompressed greyscale TIFFs created from\n" +
			"     microfilm.\n";

		BagInfoTxt bagInfo = factory.createBagInfoTxt(new StringBagFile(constants.getBagInfoTxt(), bagInfoTxtStr));
		assertEquals("Spengler University", bagInfo.getSourceOrganization());
		assertEquals("1400 Elm St., Cupertino, California, 95014", bagInfo.getOrganizationAddress());
		assertEquals("Edna Janssen", bagInfo.getContactName());
		assertEquals("+1 408-555-1212", bagInfo.getContactPhone());
		assertEquals("ej@spengler.edu", bagInfo.getContactEmail());
		assertEquals("Uncompressed greyscale TIFF images from the Yoshimuri papers collection.", bagInfo.getExternalDescription());
		assertEquals("2008-01-15", bagInfo.getBaggingDate());
		assertEquals("spengler_yoshimuri_001", bagInfo.getExternalIdentifier());
		assertEquals("260 GB", bagInfo.getBagSize());
		assertEquals("spengler_yoshimuri", bagInfo.getBagGroupIdentifier());
		assertEquals("1 of 15", bagInfo.getBagCount());
		assertEquals("/storage/images/yoshimuri", bagInfo.getInternalSenderIdentifier());
		assertEquals("Uncompressed greyscale TIFFs created from microfilm.", bagInfo.getInternalSenderDescription());
		
	}

	@Test
	public void testCaseInsensitive() {
		String bagInfoTxtStr = 
			"SOURCE-ORGANIZATION: Spengler University\n" +
			"organization-address: 1400 Elm St., Cupertino, California, 95014\n" +
			"CoNTact-NaMe: Edna Janssen\n";

		BagInfoTxt bagInfo = factory.createBagInfoTxt(new StringBagFile(constants.getBagInfoTxt(), bagInfoTxtStr));
		assertEquals("Spengler University", bagInfo.getSourceOrganization());
		assertEquals("1400 Elm St., Cupertino, California, 95014", bagInfo.getOrganizationAddress());
		assertEquals("Edna Janssen", bagInfo.getContactName());		
	}

	@Test
	public void testBaggingDate() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setBaggingDate("2008-01-15");
		assertEquals("2008-01-15", bagInfo.getBaggingDate());
		Calendar baggingDate = Calendar.getInstance();
		baggingDate.setTime(bagInfo.getBaggingDateObj());
		assertEquals(2008, baggingDate.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, baggingDate.get(Calendar.MONTH));
		assertEquals(15, baggingDate.get(Calendar.DAY_OF_MONTH));
		
		baggingDate.set(Calendar.DAY_OF_MONTH, 16);
		bagInfo.setBaggingDate(baggingDate.getTime());
		assertEquals("2008-01-16", bagInfo.getBaggingDate());
		
		bagInfo.setBaggingDate(2008, 1, 17);
		assertEquals("2008-01-17", bagInfo.getBaggingDate());
	}
	
	@Test
	public void testBagCount() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setBagCount("1 of 15");
		assertEquals("1 of 15", bagInfo.getBagCount());
		assertEquals(Integer.valueOf(1), bagInfo.getBagInGroup());
		assertEquals(Integer.valueOf(15), bagInfo.getTotalBagsInGroup());
		
		bagInfo.setBagCount(2, 16);
		assertEquals("2 of 16", bagInfo.getBagCount());
		
		bagInfo.setBagCount(3, BagInfoTxt.UNKNOWN_TOTAL_BAGS_IN_GROUP);
		assertEquals("3 of ?", bagInfo.getBagCount());
	}
	
	@Test(expected=ParseException.class)
	public void testBadBagCount() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setBagCount(" of 15");
		bagInfo.getBagInGroup();
	}

	@Test(expected=ParseException.class)
	public void testBadBagCount2() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setBagCount("1 of ");
		bagInfo.getTotalBagsInGroup();
	}

	@Test(expected=ParseException.class)
	public void testBadBagCount3() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setBagCount("1 x 15");
		bagInfo.getBagInGroup();
	}
	
}
