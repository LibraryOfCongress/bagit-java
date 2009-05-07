package gov.loc.repository.bagit.writer;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Cancellable;

public interface Writer extends Cancellable {
	/*
	 * Write the bag.
	 * @param	Bag	the bag to be written
	 * @return		the newly-written bag
	 */
	Bag write(Bag bag);
}
