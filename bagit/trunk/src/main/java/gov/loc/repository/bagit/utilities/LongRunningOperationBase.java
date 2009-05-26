package gov.loc.repository.bagit.utilities;

import java.util.ArrayList;

import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;

public class LongRunningOperationBase implements Cancellable, ProgressListenable 
{
	private CancelIndicator cancelIndicator = null;
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();

	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;
	}

	@Override
	public CancelIndicator getCancelIndicator() {
		return this.cancelIndicator;
	}

	@Override
	public void addProgressListener(ProgressListener progressListener) {
		this.progressListeners.add(progressListener);
	}
	
	@Override
	public void removeProgressListener(ProgressListener progressListener) {
		this.progressListeners.remove(progressListener);
	}
	
	protected boolean isCancelled()
	{
		return this.cancelIndicator != null && this.cancelIndicator.performCancel();
	}
	
	protected void progress(String activity, String item, int count, int total)
	{
		for (ProgressListener listener : this.progressListeners)
		{
			listener.reportProgress(activity, item, count, total);
		}
	}
}
