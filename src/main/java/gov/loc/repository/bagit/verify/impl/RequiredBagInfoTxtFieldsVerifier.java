package gov.loc.repository.bagit.verify.impl;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.Verifier;

public class RequiredBagInfoTxtFieldsVerifier implements Verifier {

	public static final String CODE_MISSING_BAGINFOTXT = "missing_baginfotxt";
	public static final String CODE_MISSING_REQUIRED_FIELD = "missing_required_field";
	
	private String[] requiredFields;
	
	public RequiredBagInfoTxtFieldsVerifier(String[] requiredFields) {
		this.requiredFields = requiredFields;
	}
	
	public SimpleResult verify(Bag bag) {
		SimpleResult result = new SimpleResult(true);
		
		BagInfoTxt bagInfo = bag.getBagInfoTxt();
		if (bagInfo == null) {
			result.setSuccess(false);
			result.addMessage(CODE_MISSING_BAGINFOTXT, "Bag-info.txt is missing");
		} else {
			for(String field : this.requiredFields) {
				String value = bagInfo.get(field);
				if (value == null || value.length() == 0) {
					result.setSuccess(false);
					result.addMessage(CODE_MISSING_REQUIRED_FIELD, "Required field {0} is not provided.", field);
				}
			}
		}
		
		return result;
		
	}

}
