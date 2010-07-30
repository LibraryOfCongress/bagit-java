package gov.loc.repository.bagit.transformer;

import gov.loc.repository.bagit.Bag;

public interface Completer {

	Bag complete(Bag bag);
}
