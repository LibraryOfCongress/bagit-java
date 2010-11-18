package gov.loc.repository.bagit.v0_96;

import static org.junit.Assert.*;

import java.text.ParseException;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.StringBagFile;

import org.junit.Test;


public class BagInfoTxtImplTest {
	BagPartFactory factory = BagFactory.getBagPartFactory(Version.V0_96);
	BagConstants constants = BagFactory.getBagConstants(Version.V0_96);

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
			"Bagging-Date: 2008-01-15\n" +
			"External-Identifier: spengler_yoshimuri_001\n" +
			"Bag-Size: 260 GB\n" +
			"Payload-Oxum: 279164409832.1198\n" +
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
		//This changed from v0.95
		assertEquals("2008-01-15", bagInfo.getBaggingDate());
		assertEquals("spengler_yoshimuri_001", bagInfo.getExternalIdentifier());
		//This changed from v0.95
		assertEquals("260 GB", bagInfo.getBagSize());
		//This was added
		assertEquals("279164409832.1198", bagInfo.getPayloadOxum());
		assertEquals("spengler_yoshimuri", bagInfo.getBagGroupIdentifier());
		assertEquals("1 of 15", bagInfo.getBagCount());
		assertEquals("/storage/images/yoshimuri", bagInfo.getInternalSenderIdentifier());
		assertEquals("Uncompressed greyscale TIFFs created from microfilm.", bagInfo.getInternalSenderDescription());
		
	}
	
	@Test
	public void testPayloadOxum() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setPayloadOxum("279164409832.1198");
		assertEquals("279164409832.1198", bagInfo.getPayloadOxum());
		assertEquals(Long.valueOf(279164409832L), bagInfo.getOctetCount());
		assertEquals(Long.valueOf(1198L), bagInfo.getStreamCount());
		
		bagInfo.setPayloadOxum(279164409833L, 1199L);
		assertEquals("279164409833.1199", bagInfo.getPayloadOxum());
	}

	@Test(expected=ParseException.class)
	public void testBadPayloadOxum() throws Exception {
		BagInfoTxt bagInfo = factory.createBagInfoTxt();
		bagInfo.setPayloadOxum("279164409832");
		bagInfo.getOctetCount();
	}
}
