package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.BagVerifyResult;

public interface Verifier {	
	BagVerifyResult verify(Bag bag);
}
