package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.SimpleResult;

public interface Verifier {	
	SimpleResult verify(Bag bag);
}
