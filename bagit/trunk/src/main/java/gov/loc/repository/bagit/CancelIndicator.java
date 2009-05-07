package gov.loc.repository.bagit;

public interface CancelIndicator {
	/*
	 * Indicates whether the Cancellable should cancel the operation.
	 */
	boolean performCancel();
}
