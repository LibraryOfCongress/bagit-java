package gov.loc.repository.bagit.verify;

public interface CompleteVerifier extends Verifier {
	public void setMissingBagItTolerant(boolean missingBagItTolerant);
	public void setAdditionalDirectoriesInBagDirTolerant(boolean additionalDirectoriesInBagDirTolerant);
}
