package gov.loc.repository.bagit.verify.impl;

import java.text.MessageFormat;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.Verifier;

public class RequiredBagInfoTxtFieldsVerifier implements Verifier {

	private String[] requiredFields;
	
	public RequiredBagInfoTxtFieldsVerifier(String[] requiredFields) {
		this.requiredFields = requiredFields;
	}
	
	public SimpleResult verify(Bag bag) {
		SimpleResult result = new SimpleResult(true);
		
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		if (bagInfo == null) {
			result.setSuccess(false);
			result.addMessage("Bag-info.txt is missing");
		} else {
			for(String field : this.requiredFields) {
				String value = bagInfo.get(field);
				if (value == null || value.length() == 0) {
					result.setSuccess(false);
					result.addMessage(MessageFormat.format("Required field {0} is not provided.", field));
				}
			}
		}
		
		return result;
		
	}

}
