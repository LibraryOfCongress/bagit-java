package gov.loc.repository.bagit.verify.impl;

import java.util.EnumSet;

import gov.loc.repository.bagit.verify.Verifier;
import gov.loc.repository.bagit.verify.VerifyOption;

public abstract class AbstractVerifier implements Verifier {
	private EnumSet<VerifyOption> options = null;
	
	@Override
	public EnumSet<VerifyOption> getOptions() {
		return EnumSet.copyOf(this.options);
	}
	
	@Override
	public void setOptions(EnumSet<VerifyOption> options) {
		this.options = EnumSet.copyOf(options);
	}
	
	protected boolean isSet(VerifyOption option) {
		if (this.options == null) {
			return false;
		}
		return this.options.contains(option);
	}
}
