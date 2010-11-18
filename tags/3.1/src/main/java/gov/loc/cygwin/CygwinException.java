package gov.loc.cygwin;

public class CygwinException extends Exception
{
	private static final long serialVersionUID = 1L;

	public CygwinException()
	{
		super();
	}

	public CygwinException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CygwinException(String message)
	{
		super(message);
	}

	public CygwinException(Throwable cause)
	{
		super(cause);
	}
}
