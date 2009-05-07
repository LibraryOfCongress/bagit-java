package gov.loc.repository.bagit.writer;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressMonitorable;

public interface Writer extends Cancellable, ProgressMonitorable {
	/*
	 * Write the bag.
	 * @param	Bag	the bag to be written
	 * @return		the newly-written bag
	 */
	Bag write(Bag bag);
}
