package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;

public class CancelUtil 
{
	// Private constructor to prevent instantiation.
	private CancelUtil() {}	
	
	public static boolean isCancelled(Object o)
	{
		boolean isCancelled = false;
		
		if (o != null && o instanceof Cancellable)
		{
			CancelIndicator indicator = ((Cancellable)o).getCancelIndicator();
			
			if (indicator != null)
			{
				isCancelled = indicator.performCancel();
			}
		}
		
		return isCancelled;
	}
}
