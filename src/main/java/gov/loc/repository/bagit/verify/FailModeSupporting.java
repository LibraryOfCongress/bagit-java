package gov.loc.repository.bagit.verify;

public interface FailModeSupporting {
	public enum FailMode { 
		/*
		 * Fail on first error.
		 */
		FAIL_FAST,
		/*
		 * Fail at end.
		 */
		FAIL_SLOW,
		/*
		 * Fail after each step of verification.
		 * A step is a set of like verification operations.
		 * For example, check that all payload files are in at least one manifest.
		 */
		FAIL_STEP,
		/*
		 * Fail after each stage of verification.
		 * A stage is a set of logically grouped verification operations.
		 * For example, when validating a bag, all of the operations to verify
		 * that a bag is complete is a stage.
		 * 
		 */
		FAIL_STAGE };
	
	void setFailMode(FailMode failMode);
	FailMode getFailMode();
}
