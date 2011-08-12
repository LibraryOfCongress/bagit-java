package gov.loc.repository.bagit.progresslistener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gov.loc.repository.bagit.ProgressListener;

public class CompositeProgressListener implements ProgressListener {
	private List<ProgressListener> listeners = Collections.synchronizedList(new ArrayList<ProgressListener>());
	
	public CompositeProgressListener() {
	}

	public CompositeProgressListener(Collection<ProgressListener> listeners) {
		this.listeners.addAll(listeners);
	}

	public CompositeProgressListener(ProgressListener[] listeners) {
		this.listeners.addAll(Arrays.asList(listeners));
	}
	
	public List<ProgressListener> getProgressListeners() {
		return Collections.unmodifiableList(this.listeners);
	}
	
	public void addProgressListener(ProgressListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeProgressListener(ProgressListener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	public void reportProgress(String activity, Object item, Long count,
			Long total) {
		for(ProgressListener listener : this.listeners) {
			listener.reportProgress(activity, item, count, total);
		}
	}

}
