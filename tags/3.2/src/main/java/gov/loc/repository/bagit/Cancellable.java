package gov.loc.repository.bagit;

public interface Cancellable
{
	void cancel();
	boolean isCancelled();
}
