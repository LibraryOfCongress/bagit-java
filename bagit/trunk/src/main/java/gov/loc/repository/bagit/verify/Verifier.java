package gov.loc.repository.bagit.verify;

import java.util.EnumSet;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.SimpleResult;

public interface Verifier {
	SimpleResult verify(Bag bag);
	
	/**
	 * Gets the {@link VerifyOption options} this verifier
	 * will use when processing the bag.
	 * 
	 * @return The set of current processing options.
	 */
	EnumSet<VerifyOption> getOptions();

	/**
	 * Sets the {@link VerifyOption options} this verifier
	 * will use when processing the bag.  The options should
	 * be set prior to calling the {@link #verify(Bag)} method.
	 *   
	 * @param options Sets the processing options.
	 */
	void setOptions(EnumSet<VerifyOption> options);
}
