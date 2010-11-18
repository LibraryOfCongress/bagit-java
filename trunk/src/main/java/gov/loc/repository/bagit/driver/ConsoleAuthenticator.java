package gov.loc.repository.bagit.driver;

import java.io.Console;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Arrays;

class ConsoleAuthenticator extends Authenticator
{
	private String username;
	private char[] password;
	
	public ConsoleAuthenticator(String username)
	{
		this.username = username;
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
		PasswordAuthentication credentials;
		Console console = System.console();
		
		if (console == null)
		{
			credentials = null;
		}
		else
		{
			if (this.username == null)
			{
				this.username = console.readLine("Username: ");
			}
			
			if (this.password == null)
			{
				this.password = console.readPassword("Password: ");
			}
			
			credentials = new PasswordAuthentication(this.username, this.password);
		}
		
		return credentials;
	}
}
