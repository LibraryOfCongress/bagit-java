package gov.loc.repository.bagit;

public interface ProgressListener
{
	void reportProgress(String activity, Object item, Long count, Long total);
}
