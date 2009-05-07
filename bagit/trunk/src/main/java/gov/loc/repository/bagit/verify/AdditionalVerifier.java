package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.SimpleResult;

public interface AdditionalVerifier {
	SimpleResult verify(Bag bag);
}
