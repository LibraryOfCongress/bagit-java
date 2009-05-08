package gov.loc.repository.bagit.verify;

public interface CompleteVerifier extends Verifier {
	void setMissingBagItTolerant(boolean missingBagItTolerant);
}
