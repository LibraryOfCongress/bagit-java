package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.util.ArrayList;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.ProgressListener;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.impl.AbstractBagVisitor;
import gov.loc.repository.bagit.utilities.TempFileHelper;
import gov.loc.repository.bagit.writer.Writer;

public abstract class AbstractWriter extends AbstractBagVisitor implements Writer {

	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	protected BagFactory bagFactory;
	
	public AbstractWriter(BagFactory bagFactory) {
		this.bagFactory = bagFactory;
	}
	
	@Override
	public void addProgressListener(ProgressListener progressListener) {
		this.progressListeners.add(progressListener);
	}

	@Override
	public void removeProgressListener(ProgressListener progressListener) {
		this.progressListeners.remove(progressListener);
	}
	
	protected void progress(String activity, String item, long count, long total)
	{
		for (ProgressListener listener : this.progressListeners)
		{
			listener.reportProgress(activity, item, count, total);
		}
	}

	protected File getTempFile(File file) {
		return TempFileHelper.getTempFile(file);
	}

	protected abstract Format getFormat();
	
	protected void switchTemp(File file) {
		TempFileHelper.switchTemp(file);

	}
}
