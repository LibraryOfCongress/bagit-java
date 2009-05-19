package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;

public class LongRunningOperationBase implements Cancellable, ProgressListenable 
{
	private CancelIndicator cancelIndicator = null;
	private ProgressListener progressListener = null;

	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;
	}

	@Override
	public CancelIndicator getCancelIndicator() {
		return this.cancelIndicator;
	}

	@Override
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	protected boolean isCancelled()
	{
		return this.cancelIndicator != null && this.cancelIndicator.performCancel();
	}
	
	protected void progress(String activity, String item, int count, int total)
	{
		if (this.progressListener != null)
		{
			this.progressListener.reportProgress(activity, item, count, total);
		}
	}
}
