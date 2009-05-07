package gov.loc.repository.bagit.bag;

import gov.loc.repository.bagit.CancelIndicator;

public class DummyCancelIndicator implements CancelIndicator {

	private long count = 0;
	private long calls = 0;
	
	public DummyCancelIndicator(long count) {
		this.count = 0;
	}
	
	@Override
	public boolean performCancel() {
		calls++;
		if (calls >= count) {
			return true;
		}
		return false;
	}

}
