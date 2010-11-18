package gov.loc.repository.bagit.impl;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.StringBagFile;

import org.junit.Test;


public abstract class AbstractBagInfoTxtImplTest {

	public BagPartFactory factory = BagFactory.getBagPartFactory(this.getVersion());
	public BagConstants constants = BagFactory.getBagConstants(this.getVersion());

	public abstract Version getVersion();	
	
	@Test
	public void testBagInfoTxt() {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), this.getTestBagInfoTxtBagInfoTxtString()));
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
		this.addlTestBagInfoTxt(bagInfo);
		
		
	}
	public abstract String getTestBagInfoTxtBagInfoTxtString();	
	public void addlTestBagInfoTxt(BagInfoTxt bagInfo) {}
	
	@Test
	public void testCaseInsensitive() {
		String bagInfoTxtStr = 
			"SOURCE-ORGANIZATION: Spengler University\n" +
			"organization-address: 1400 Elm St., Cupertino, California, 95014\n" +
			"CoNTact-NaMe: Edna Janssen\n";

		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), bagInfoTxtStr));
		assertEquals("Spengler University", bagInfo.getSourceOrganization());
		assertEquals("1400 Elm St., Cupertino, California, 95014", bagInfo.getOrganizationAddress());
		assertEquals("Edna Janssen", bagInfo.getContactName());		
	}

	@Test
	public void testBaggingDate() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
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
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
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
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
		bagInfo.setBagCount(" of 15");
		bagInfo.getBagInGroup();
	}

	@Test(expected=ParseException.class)
	public void testBadBagCount2() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
		bagInfo.setBagCount("1 of ");
		bagInfo.getTotalBagsInGroup();
	}

	@Test(expected=ParseException.class)
	public void testBadBagCount3() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
		bagInfo.setBagCount("1 x 15");
		bagInfo.getBagInGroup();
	}
	
	@Test
	public void testGetStandardFields() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), this.getTestBagInfoTxtBagInfoTxtString()));
		bagInfo.put("foo", "bar");
		List<String> fields = bagInfo.getStandardFields();
		assertTrue(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_EMAIL));
		assertTrue(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_NAME));
		assertTrue(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_PHONE));		
		assertFalse(fields.contains("foo"));
	}

	@Test
	public void testGetNonstandardFields() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), this.getTestBagInfoTxtBagInfoTxtString()));
		bagInfo.put("foo", "bar");
		List<String> fields = bagInfo.getNonstandardFields();
		assertFalse(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_EMAIL));
		assertTrue(fields.contains("foo"));
		assertEquals(1, fields.size());
	}

}
