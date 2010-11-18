package gov.loc.repository.bagit.driver;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Arrays;

class ConstantCredentialsAuthenticator extends Authenticator
{
	private String username;
	private char[] password;
	
	public ConstantCredentialsAuthenticator(String username, String password)
	{
		this.username = username;
		this.password = password.toCharArray();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		if (this.password != null)
		{
			Arrays.fill(this.password, 'x');
		}
		
		super.finalize();
	}
	
	@Override
	protected synchronized PasswordAuthentication getPasswordAuthentication() 
	{
		return new PasswordAuthentication(this.username, this.password);
	}
}
