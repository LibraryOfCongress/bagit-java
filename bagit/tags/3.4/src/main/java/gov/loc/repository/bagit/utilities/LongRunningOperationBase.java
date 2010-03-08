package gov.loc.repository.bagit.utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.ProgressListener;

public class LongRunningOperationBase implements Cancellable, ProgressListenable 
{
	private boolean isCancelled = false;
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	private Set<ProgressListenable> chainedProgressListenables = new HashSet<ProgressListenable>();
	private Set<Cancellable> chainedCancellables = new HashSet<Cancellable>();
	
	@Override
	public void addProgressListener(ProgressListener progressListener) {
		this.progressListeners.add(progressListener);
		for(ProgressListenable progressListenable : this.chainedProgressListenables) {
			progressListenable.addProgressListener(progressListener);
		}
	}
	
	@Override
	public void removeProgressListener(ProgressListener progressListener) {
		this.progressListeners.remove(progressListener);
		for(ProgressListenable progressListenable : this.chainedProgressListenables) {
			progressListenable.removeProgressListener(progressListener);
		}
	}
	
	@Override
	public void cancel()
	{
		this.isCancelled = true;
		for(Cancellable cancellable : this.chainedCancellables) {
			cancellable.cancel();
		}
	}
	
	@Override
	public boolean isCancelled()
	{
		return this.isCancelled;
	}
	
	protected void addChainedProgressListenable(ProgressListenable progressListenable) {
		this.chainedProgressListenables.add(progressListenable);
	}
	
	protected void removeChainedProgressListenable(ProgressListenable progressListenable) {
		this.chainedProgressListenables.remove(progressListenable);
	}
	
	protected void addChainedCancellable(Cancellable cancellable) {
		this.chainedCancellables.add(cancellable);
	}
	
	protected void removeChainedCancellable(Cancellable cancellable) {
		this.chainedCancellables.remove(cancellable);
	}
	
	protected void progress(String activity, Object item)
	{
		this.progress(activity, item, (Long)null, (Long)null);
	}
	
	protected void progress(String activity, Object item, Long count, Long total)
	{
		for (ProgressListener listener : this.progressListeners)
		{
			listener.reportProgress(activity, item, count, total);
		}
	}
	
	protected void progress(String activity, Object item, Integer count, Integer total)
	{
		this.progress(activity, item, count == null? (Long)null : new Long(count), total == null? (Long)null : new Long(total));
	}
	
	protected void delegateProgress(ProgressListenable listenable)
	{
		listenable.addProgressListener(new ProgressDelegate());
	}
	
	private class ProgressDelegate implements ProgressListener
	{
		@Override
		public void reportProgress(String activity, Object item, Long count, Long total)
		{
			LongRunningOperationBase.this.progress(activity, item, count, total);
		}		
	}
}
