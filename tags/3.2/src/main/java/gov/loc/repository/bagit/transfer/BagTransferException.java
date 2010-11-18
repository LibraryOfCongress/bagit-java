package gov.loc.repository.bagit.transfer;

public class BagTransferException extends Exception
{
    private static final long serialVersionUID = 1L;

    public BagTransferException()
    {
    }

    public BagTransferException(String message)
    {
        super(message);
    }

    public BagTransferException(Throwable cause)
    {
        super(cause);
    }

    public BagTransferException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
