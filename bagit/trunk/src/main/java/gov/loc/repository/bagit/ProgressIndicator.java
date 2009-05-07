package gov.loc.repository.bagit;

public interface ProgressIndicator {
	void reportProgress(String activity, String item, int count, int total);
}
