package gov.loc.repository.bagit.verify.impl;

import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.Verifier;

public class PayloadOxumVerifier implements Verifier {

	public static final String CODE_INCORRECT_PAYLOAD_OXUM = "incorrect_payload-oxum";
	
	@Override
	public SimpleResult verify(Bag bag) {
		SimpleResult result = new SimpleResult(true);
		if (bag.getBagInfoTxt() == null) {
			result.addWarningMessage("Bag does not have a bag-info.txt");
			return result;
		}
		String checkOxum = bag.getBagInfoTxt().getPayloadOxum();
		if (checkOxum == null) {
			result.addWarningMessage("Bag-info.txt does not have a Payload-Oxum field");
			return result;
		}

		String genOxum = BagHelper.generatePayloadOxum(bag);
		if (! checkOxum.equals(genOxum)) {
			result.setSuccess(false);
			result.addMessage(CODE_INCORRECT_PAYLOAD_OXUM, 
					MessageFormat.format("Expected payload-oxum {0}, but found payload-oxum {1}", checkOxum, genOxum));
		}
		return result;
	}

}
