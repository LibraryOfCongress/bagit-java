package gov.loc.repository.bagit.verify.impl;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.Verifier;
import static gov.loc.repository.bagit.verify.CompleteVerifier.*;

/**
 * Verifies the structure of a bag, but ignores the payload directory.
 * This verifier can be used prior to fetching a holey bag, and will
 * ensure that the bag is structured correctly.
 */
public class ValidHoleyBagVerifier extends LongRunningOperationBase implements Verifier
{
	public static final String CODE_MISSING_FETCHTXT = "missing_fetchtxt";
	
	private static final Log log = LogFactory.getLog(ValidHoleyBagVerifier.class);
		
	@Override
	public SimpleResult verify(Bag bag) 
	{
		SimpleResult result = new SimpleResult(true);
		
		log.trace("Checking for bag declaration.");
		if (bag.getBagItTxt() == null)
			result.setSuccess(false);
			result.addMessage(CODE_NO_BAGITTXT, MessageFormat.format("Bag does not have {0}.", bag.getBagConstants().getBagItTxt()));				

		log.trace("Checking for at least one payload manifest.");
		if (bag.getPayloadManifests().isEmpty()) {
			result.setSuccess(false);
			result.addMessage(CODE_NO_PAYLOAD_MANIFEST, "Bag does not have any payload manifests.");
		}
		
		log.trace("Confirming version specified matches version in declaration.");
		if (bag.getBagItTxt() != null && !bag.getBagConstants().getVersion().versionString.equals(bag.getBagItTxt().getVersion())) {
			result.setSuccess(false);
			result.addMessage(CODE_WRONG_VERSION, MessageFormat.format("Version is not {0}.", bag.getBagConstants().getVersion()));				
		}
		
		log.trace("Checking for fetch.txt.");
		if (bag.getFetchTxt() == null)
			result.setSuccess(false);
			result.addMessage(CODE_MISSING_FETCHTXT, MessageFormat.format("Bag does not have {0}.", bag.getBagConstants().getFetchTxt()));
		
		log.info("Completion check: " + result.toString());
		
		return result;
	}
	
	
}
