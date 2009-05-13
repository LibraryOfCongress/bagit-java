package gov.loc.repository.bagit;

public interface Cancellable {
	void setCancelIndicator(CancelIndicator cancelIndicator);
	CancelIndicator getCancelIndicator();
}
