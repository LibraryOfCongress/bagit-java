package gov.loc.repository.bagit.writer;

import java.io.File;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;

public interface Writer extends Cancellable, ProgressListenable {
	/*
	 * Write the bag.
	 * @param	Bag	the bag to be written
	 * @return		the newly-written bag
	 */
	Bag write(Bag bag, File file);
}
