package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.ActiveCancellable;
import gov.loc.repository.bagit.CancelIndicator;

public class ActiveCancelIndicator implements CancelIndicator, ActiveCancellable
{
	private boolean isCancelled = false;
	
	@Override
	public boolean performCancel()
	{
		return this.isCancelled;
	}

	@Override
	public void cancel()
	{
		this.isCancelled = true;
	}
}
