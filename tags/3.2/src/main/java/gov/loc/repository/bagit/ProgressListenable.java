package gov.loc.repository.bagit;

public interface ProgressListenable {
	void addProgressListener(ProgressListener progressListener);
	void removeProgressListener(ProgressListener progressListener);
}
