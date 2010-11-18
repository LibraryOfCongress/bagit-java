package gov.loc.repository.bagit;

public interface BagVisitor {
	
	public void startBag(Bag bag);	
	public void startTags();
	public void visitTag(BagFile bagFile);
	public void endTags();
	public void startPayload();
	public void visitPayload(BagFile bagFile);
	public void endPayload();
	public void endBag();
	
}
