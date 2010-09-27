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
 * <p>Fetch failures are used by the the
 * {@link BagFetcher#setFetchFailStrategy(FetchFailStrategy) setFetchFailStrategy}
 * method of the <c>BagFetcher</c>.  Some common strategies are
 * the {@link StandardFailStrategies#ALWAYS_CONTINUE ALWAYS_CONTINUE},
 * {@link StandardFailStrategies#ALWAYS_RETRY ALWAYS_RETRY},
 * and {@link StandardFailStrategies#FAIL_FAST FAIL_FAST} strategies.
 * A more complex strategy might involve the
 * {@link ThresholdFailStrategy}.</p>
 * 
 * @version $id$
 * @see FetchFailureAction
 * @see StandardFailStrategies
 * @see ThresholdFailStrategy
 * @see BagFetcher
 */
public interface FetchFailStrategy
{
	FetchFailureAction registerFailure(FetchTarget target, Object context);
}
