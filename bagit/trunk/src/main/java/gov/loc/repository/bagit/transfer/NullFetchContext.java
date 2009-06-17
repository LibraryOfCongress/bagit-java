package gov.loc.repository.bagit.transfer;

import java.net.PasswordAuthentication;

/**
 * A very naieve implementation of a {@link FetchContext}.
 * This implementation always returns <c>false</c> for
 * {@link #performCancel() performCancel()}, and does
 * nothing on {@link #reportProgress(String, String, int, int) reportProgress()}.
 * 
 * @version $Id$
 *
 */
public class NullFetchContext implements FetchContext
{
	public boolean requiresLogin()
	{
		return false;
	}
	
	public PasswordAuthentication getCredentials()
	{
		return null;
	}
	
	public boolean performCancel()
	{
		return false;
	}

	public void reportProgress(String activity, String item, int count, int total)
	{
	}
}
