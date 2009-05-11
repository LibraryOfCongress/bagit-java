package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.impl.AbstractBagVisitor;
import gov.loc.repository.bagit.writer.Writer;

public abstract class AbstractWriter extends AbstractBagVisitor implements Writer {

	protected CancelIndicator cancelIndicator = null;
	protected ProgressListener progressIndicator = null;
	protected BagFactory bagFactory;
	
	public AbstractWriter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
	}
	
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;		
	}
	
	@Override
	public void setProgressIndicator(ProgressListener progressIndicator) {
		this.progressIndicator = progressIndicator;
	}

}
