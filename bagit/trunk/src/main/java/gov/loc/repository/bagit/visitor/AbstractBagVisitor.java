package gov.loc.repository.bagit.visitor;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagVisitor;

public abstract class AbstractBagVisitor implements BagVisitor {

	@Override
	public void endBag() {
	}

	@Override
	public void endPayload() {
	}

	@Override
	public void endTags() {
	}

	@Override
	public void startBag(Bag bag) {
	}

	@Override
	public void startPayload() {
	}

	@Override
	public void startTags() {
	}

	@Override
	public void visitPayload(BagFile bagFile) {
	}

	@Override
	public void visitTag(BagFile bagFile) {
	}

}
