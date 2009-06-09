package gov.loc.repository.bagit.utilities;

import java.util.ArrayList;

import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;

public class LongRunningOperationBase implements Cancellable, ProgressListenable 
{
	private boolean isCancelled = false;
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();

	@Override
	public void addProgressListener(ProgressListener progressListener) {
		this.progressListeners.add(progressListener);
	}
	
	@Override
	public void removeProgressListener(ProgressListener progressListener) {
		this.progressListeners.remove(progressListener);
	}
	
	@Override
	public void cancel()
	{
		this.isCancelled = true;
	}
	
	@Override
	public boolean isCancelled()
	{
		return this.isCancelled;
	}
	
	protected void progress(String activity, String item, long count, long total)
	{
		for (ProgressListener listener : this.progressListeners)
		{
			listener.reportProgress(activity, item, count, total);
		}
	}
}
