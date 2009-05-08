package gov.loc.repository.bagit.bag;

import java.text.MessageFormat;

import gov.loc.repository.bagit.CancelIndicator;

public class DummyCancelIndicator implements CancelIndicator {

	private long count = 0;
	private volatile long calls = 0;
	
	public DummyCancelIndicator(long count) {
		this.count = count;
	}
	
	@Override
	public boolean performCancel() {
		calls++;
		System.out.println(MessageFormat.format("DummyCancelIndicator called {0} of {1}.", calls, count));
		if (calls >= count) {
			return true;
		}
		return false;
	}

}
