package gov.loc.repository.bagit;

/**
 * Receives progress reports from other components.
 * This interface is implemented by any components that receive progress
 * updates during a potentially long-running operation.</p>
 * 
 * <p>To receive progress
 * updates, pass an implementation of the the {@link ProgressListener}
 * interface to the {@link #addProgressListener(ProgressListener)} method.
 * If you no longer wish to receive updates, object may be passed to the
 * {@link #removeProgressListener(ProgressListener)} method.</p>
 * 
 * <p>It is the
 * responsibility of the listener implementations to ensure that concurrent
 * invocations of the {@link ProgressListener#reportProgress(String, Object, Long, Long)}
 * method are thread-safe.</p> 
 *
 * @see ProgressListenable
 * @see gov.loc.repository.bagit.utilities.LongRunningOperationBase
 */
public interface ProgressListener
{
	/**
	 * Receives a progress report.
	 * 
	 * @param activity Describes the current activity of the operation.  Will never be null.
	 * @param item The item currently being processed.  May be null.
	 * @param count The index of the current item being processed.  May be null.
	 * @param total The total number of items to be processed.  May be null.
	 */
	void reportProgress(String activity, Object item, Long count, Long total);
}
