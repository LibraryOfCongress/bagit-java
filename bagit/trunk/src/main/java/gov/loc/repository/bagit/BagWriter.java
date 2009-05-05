package gov.loc.repository.bagit;

public interface BagWriter extends BagVisitor {
	/*
	 * Returns the just-written Bag.
	 */
	Bag getWrittenBag();
}
