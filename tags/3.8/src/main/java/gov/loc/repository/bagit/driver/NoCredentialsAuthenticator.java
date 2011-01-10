package gov.loc.repository.bagit.driver;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class NoCredentialsAuthenticator extends Authenticator
{
	@Override
	protected PasswordAuthentication getPasswordAuthentication()
	{
		return null;
	}
}
