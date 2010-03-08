package gov.loc.repository.bagit.v0_94;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractBagInfoTxtImplTest;


public class BagInfoTxtImplTest extends AbstractBagInfoTxtImplTest {

	@Override
	public Version getVersion() {
		return Version.V0_94;
	}
	
	@Override
	public String getTestBagInfoTxtBagInfoTxtString() {
		return "Source-Organization: Spengler University\n" +
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

	}	
}
