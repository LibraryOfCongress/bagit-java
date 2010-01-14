package gov.loc.repository.bagit.v0_95.impl;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractBagConstants;

public class BagConstantsImpl extends AbstractBagConstants {
	public static final Version VERSION = Version.V0_95;
	public static final String PACKAGEINFO_TXT = "package-info.txt";
	
	public Version getVersion() {
		return VERSION;
	}

	@Override
	public String getBagInfoTxt() {
		return PACKAGEINFO_TXT;
	}
	
}
