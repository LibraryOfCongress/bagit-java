package gov.loc.repository.bagit.verify;

import java.util.List;

public interface CompleteVerifier extends Verifier {
	public void setMissingBagItTolerant(boolean missingBagItTolerant);
	public void setAdditionalDirectoriesInBagDirTolerant(boolean additionalDirectoriesInBagDirTolerant);
	public void setIgnoreAdditionalDirectories(List<String> dirs);
	public void setIgnoreSymlinks(boolean ignore);
}
