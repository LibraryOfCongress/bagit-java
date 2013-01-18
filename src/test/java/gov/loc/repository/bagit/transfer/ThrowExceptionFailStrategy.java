package gov.loc.repository.bagit.transfer;

import gov.loc.repository.bagit.FetchTxt;

/**
 * A failure strategy that immediately throws a {@link RuntimeException}.
 * This is quite useful for unit testing, and perhaps other things.
 * If the context is a {@link Throwable}, it will be thrown
 * as the {@link Throwable#getCause() cause}.
 * 
 * @version $Id$
 */
public class ThrowExceptionFailStrategy implements FetchFailStrategy 
{
	/**
	 * Throws a {@link RuntimeException}.
	 * 
	 * @param uri Put in the error message.
	 * @param size Ignored.
	 * @param context If a {@link Throwable}, the context will be passed
	 * 		  as the {@link Throwable#getCause() cause}.  Otherwise, ignored.
	 * 
	 * @throws RuntimeException Always.
	 */
	@Override
	public FetchFailureAction registerFailure(FetchTxt.FilenameSizeUrl target, Object context) 
	{
		if (context instanceof Throwable)
			throw new RuntimeException("Could not fetch: " + target.getFilename(), (Throwable)context);
		else
			throw new RuntimeException("Could not fetch: " + target.getFilename());
	}
}
