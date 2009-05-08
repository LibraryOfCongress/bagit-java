package gov.loc.repository.bagit.verify;

import java.util.EnumSet;

public final class VerifyOptions {
	private VerifyOptions() {}
	
	/**
	 * Indicates to a {@link Verifier} to perform the strictest
	 * possible processing on a bag.
	 */
	public static final EnumSet<VerifyOption> STRICT = EnumSet.noneOf(VerifyOption.class);

	/**
	 * Indicates to a {@link Verifier} to perform the least strict
	 * possible processing on a bag.
	 */
	public static final EnumSet<VerifyOption> LAX = EnumSet.allOf(VerifyOption.class);
}
