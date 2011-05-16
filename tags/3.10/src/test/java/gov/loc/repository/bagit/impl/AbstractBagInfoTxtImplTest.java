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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractBagInfoTxtImplTest {

	
	public abstract Version getVersion();	
	
	protected BagFactory bagFactory = new BagFactory();
	protected BagPartFactory factory;
	protected BagConstants constants;
	
	@Before
	public void setup() {
		this.factory = bagFactory.getBagPartFactory(this.getVersion());
		this.constants = bagFactory.getBagConstants(this.getVersion());		
	}
	
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
	public void testBagInfoTxtNullFields() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), this.getTestBagInfoTxtBagInfoTxtString()));
		bagInfo.put("Foo", null);
		String bagInfoString = new String(IOUtils.toByteArray(bagInfo.newInputStream()));
		assertFalse(bagInfoString.contains("null"));
		}
	
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
	public void testColons() {
		String bagInfoTxtStr1 = 
			"External-Description: This collection consists of six\n" +
			"   large-scale web crawls run against U.S. city web sites from May 2005\n" +
			"   to October 2007 as part of the Stanford WebBase project. Format:  ARC files\n" +
			"   generated from WebBase content.\n" +
			"External-Identifier: ark:/13030/sw1154dn7r\n";
		String bagInfoTxtStr2 = 
			"External-Description: This collection consists of six " +
			"large-scale web crawls run against U.S. city web sites from May 2005 " +
			"to October 2007 as part of the Stanford WebBase project. Format:  ARC files " +
			"generated from WebBase content.\n" +
			"External-Identifier: ark:/13030/sw1154dn7r\n";
		String bagInfoTxtStr3 = 
			"External-Description: This collection consists of six\n" +
			"   large-scale web crawls run against U.S. city web sites from May 2005\n" +
			"   to October 2007 as part of the Stanford WebBase project.\n" +
			"   Format:  ARC files\n" +
			"   generated from WebBase content.\n" +
			"External-Identifier: ark:/13030/sw1154dn7r\n";

		final String externalDescription = "This collection consists of six large-scale web crawls run against U.S. city web sites from May 2005 to October 2007 as part of the Stanford WebBase project. Format:  ARC files generated from WebBase content.";
		final String externalIdentifier = "ark:/13030/sw1154dn7r";
		BagInfoTxt bagInfo1 = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), bagInfoTxtStr1));
		assertEquals(externalDescription, bagInfo1.getExternalDescription());
		assertEquals(externalIdentifier, bagInfo1.getExternalIdentifier());
		
		BagInfoTxt bagInfo2 = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), bagInfoTxtStr2));
		assertEquals(externalDescription, bagInfo2.getExternalDescription());
		assertEquals(externalIdentifier, bagInfo2.getExternalIdentifier());

		BagInfoTxt bagInfo3 = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), bagInfoTxtStr3));
		assertEquals(externalDescription, bagInfo3.getExternalDescription());
		assertEquals(externalIdentifier, bagInfo3.getExternalIdentifier());

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
		//Standard field, but alternate capitalization
		bagInfo.remove(BagInfoTxtImpl.FIELD_BAG_SIZE);
		bagInfo.put("bag-size", "1 gb");
		List<String> fields = bagInfo.getStandardFields();
		assertTrue(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_EMAIL));
		assertTrue(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_NAME));
		assertTrue(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_PHONE));
		assertTrue(fields.contains("bag-size"));
		assertFalse(fields.contains("foo"));
	}

	@Test
	public void testGetNonstandardFields() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), this.getTestBagInfoTxtBagInfoTxtString()));
		bagInfo.put("foo", "bar");
		//Standard field, but alternate capitalization
		bagInfo.remove(BagInfoTxtImpl.FIELD_BAG_SIZE);
		bagInfo.put("bag-size", "1 gb");
		List<String> fields = bagInfo.getNonstandardFields();
		assertFalse(fields.contains(BagInfoTxtImpl.FIELD_CONTACT_EMAIL));
		assertTrue(fields.contains("foo"));
		assertFalse(fields.contains("bag-size"));
		assertEquals(1, fields.size());
	}

	@Test
	public void testDupeMapFunctions() {
		String bagInfoTxtStr = 
			"Source-Organization: Spengler University\n" +
			"Source-Organization: Mangler University\n";
			
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), bagInfoTxtStr));
		assertEquals("Spengler University", bagInfo.getSourceOrganization());
		bagInfo.put("Source-Organization", "Fangler University");
		assertEquals("Fangler University", bagInfo.getSourceOrganization());
		bagInfo.remove("Source-Organization");
		assertTrue(bagInfo.containsKey("Source-Organization"));
		assertEquals("Mangler University", bagInfo.getSourceOrganization());
		
	}

	@Test
	public void testDupeListFunctions() {
		String bagInfoTxtStr = 
			"Source-Organization: Spengler University\n" +
			"Source-Organization: Mangler University\n";
			
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt(new StringBagFile(this.constants.getBagInfoTxt(), bagInfoTxtStr));
		List<String> values = bagInfo.getList("Source-Organization");
		assertEquals(2, values.size());
		assertEquals("Spengler University", values.get(0));
		assertEquals("Mangler University", values.get(1));
		
		values = bagInfo.getSourceOrganizationList();
		assertEquals(2, values.size());
		assertEquals("Spengler University", values.get(0));
		assertEquals("Mangler University", values.get(1));
		
		bagInfo.putList("Source-Organization", "Fangler University");
		values = bagInfo.getList("Source-Organization");
		assertEquals(3, values.size());
		assertEquals("Fangler University", values.get(2));
		
		bagInfo.removeList("Source-Organization", "Mangler University");
		values = bagInfo.getList("Source-Organization");
		assertEquals(2, values.size());

		bagInfo.addSourceOrganization("Wangler University");
		values = bagInfo.getList("Source-Organization");
		assertEquals(3, values.size());
		
		bagInfo.removeAllList("Source-Organization");
		values = bagInfo.getList("Source-Organization");
		assertEquals(0, values.size());		
	}

	
}
