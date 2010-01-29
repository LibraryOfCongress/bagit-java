package gov.loc.repository.bagit;

/**
 * Allows long-running operations to be canceled.
 */
public interface Cancellable
{
	/**
	 * <p>Cancels a long-running operation.  The operation may not be
	 * terminated immediately, but may end at the earliest possible
	 * convenience of the implementor.</p>
	 * 
	 * <p>Consecutive calls to this method must not have any additional
	 * effect.</p>
	 * 
	 * <p>The {@link #isCancelled()} property must be set to return
	 * <c>true</c> by the end of the cancel method.</p>
	 * 
	 * <p>The state of the operation after a cancel is undefined by
	 * this method, but gurantees may be made by particular implementors.</p>
	 */
	void cancel();
	
	/**
	 * Whether or not the operation has been canceled.  That this method
	 * returns <c>true</c> only indicates that a request to cancel
	 * has been registered - the operation may still be performing work
	 * until some convenient time to exit is reached. 
	 * 
	 * @return Returns <c>true</c> if the {@link #cancel()} method
	 * has been called; <c>false</c> otherwise.
	 */
	boolean isCancelled();
}
