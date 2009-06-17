package gov.loc.repository.bagit.transfer;

import java.net.PasswordAuthentication;

public interface FetchContext
{
	boolean requiresLogin();
	PasswordAuthentication getCredentials();
}
