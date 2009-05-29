package gov.loc.repository.bagit.transfer;

/**
 * Represents a strategy for failing a fetch, based on some
 * implementation-defined criteria.  Implementors must
 * return a {@link FetchFailureAction} value that determines
 * the action that the caller should take in response to
 * the registered failure.
 * 
 * <p>Implementations of this interface <strong>must be
 * thread-safe</strong>.</p>
 * 
 * @version $id$
 * @see FetchFailureAction
 */
public interface FetchFailStrategy
{
	FetchFailureAction registerFailure(String uri, Long size, Object context);
}
