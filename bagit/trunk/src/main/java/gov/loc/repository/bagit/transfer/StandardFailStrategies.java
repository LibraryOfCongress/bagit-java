package gov.loc.repository.bagit.transfer;

public final class StandardFailStrategies
{
	// Prevent instantiation.
	private StandardFailStrategies() {}
	
	public static final FetchFailStrategy ALWAYS_CONTINUE = new FetchFailStrategy() {
		@Override
		public FetchFailureAction registerFailure(FetchTarget target, Object context) {
			return FetchFailureAction.CONTINUE_WITH_NEXT;
		}
	};

	public static final FetchFailStrategy FAIL_FAST = new FetchFailStrategy() {
		@Override
		public FetchFailureAction registerFailure(FetchTarget target, Object context) {
			return FetchFailureAction.STOP;
		}
	};

	public static final FetchFailStrategy ALWAYS_RETRY = new FetchFailStrategy() {
		@Override
		public FetchFailureAction registerFailure(FetchTarget target, Object context) {
			return FetchFailureAction.RETRY_CURRENT;
		}
	};
}
