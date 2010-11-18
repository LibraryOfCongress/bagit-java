package gov.loc.repository.bagit.utilities;

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
			isCancelled = ((Cancellable)o).isCancelled();
		}
		
		return isCancelled;
	}
	
	public static void cancel(Object o)
	{
		if (o != null && o instanceof Cancellable)
		{
			((Cancellable)o).cancel();
		}
	}
}
