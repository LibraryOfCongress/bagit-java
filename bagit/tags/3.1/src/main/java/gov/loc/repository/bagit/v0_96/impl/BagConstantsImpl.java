package gov.loc.repository.bagit.v0_96.impl;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractBagConstants;

public class BagConstantsImpl extends AbstractBagConstants {
	public static final Version VERSION = Version.V0_96;
		
	public Version getVersion() {
		return VERSION;
	}

	
}
