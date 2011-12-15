package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.BagVerifyResult;


public interface CompleteVerifier {
	BagVerifyResult verify(Bag bag);
	
}
