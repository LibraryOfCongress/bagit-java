package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.BagVerifyResult;

public interface ValidVerifier extends Verifier {
	BagVerifyResult verify(Bag bag);	
}
