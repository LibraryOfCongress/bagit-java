package gov.loc.repository.bagit.visitor;

public class TransferException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TransferException() {
	}

	public TransferException(String msg) {
		super(msg);
	}

	public TransferException(Throwable err) {
		super(err);
	}

	public TransferException(String msg, Throwable err) {
		super(msg, err);
	}

}
