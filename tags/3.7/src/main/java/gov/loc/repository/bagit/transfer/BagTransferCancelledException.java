package gov.loc.repository.bagit.transfer;

public class BagTransferCancelledException extends BagTransferException
{
	private static final long serialVersionUID = 1L;

	public BagTransferCancelledException()
	{
		super();
	}

	public BagTransferCancelledException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BagTransferCancelledException(String message)
	{
		super(message);
	}

	public BagTransferCancelledException(Throwable cause)
	{
		super(cause);
	}
}
