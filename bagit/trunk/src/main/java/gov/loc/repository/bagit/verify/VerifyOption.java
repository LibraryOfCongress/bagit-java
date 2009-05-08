package gov.loc.repository.bagit.verify;

import java.util.EnumSet;

/**
 * Specifies various options for bag processing to {@link Verifier verifiers}.
 * There are useful {@link EnumSet groupings} of these values in the
 * {@link VerifyOptions} class.
 * 
 * @version $Id$
 * @see VerifyOptions
 * @see Verifier
 */
public enum VerifyOption {
	/**
	 * Indicates to a {@link Verifier} that it should not fail
	 * verification if the <c>bagit.txt</c> file is missing.
	 */
	TOLERATE_MISSING_DECLARATION,
	
	/**
	 * Indicates to a {@link Verifier} that it should ignore
	 * the presence of extra hidden files in the payload.
	 */
	IGNORE_EXTRA_HIDDEN_FILES;
}
