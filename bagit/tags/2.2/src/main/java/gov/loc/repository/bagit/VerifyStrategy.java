package gov.loc.repository.bagit;

import gov.loc.repository.bagit.utilities.SimpleResult;

public interface VerifyStrategy {
	SimpleResult verify(Bag bag);
}
