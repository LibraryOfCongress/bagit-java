package gov.loc.repository.bagit;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public interface BagInfoTxt extends BagFile, Map<String, String> {
	
	static final int UNKNOWN_TOTAL_BAGS_IN_GROUP = -1;
	
	static final String UNKNOWN_TOTAL_BAGS_IN_GROUP_MARKER = "?";
	
	static final String TYPE = "BagInfoTxt";
	
	void setSourceOrganization(String sourceOrganization);
	
	void setOrganizationAddress(String organizationAddress);
	
	void setContactName(String contactName);
	
	void setContactPhone(String contactPhone);
	
	void setContactEmail(String contactEmail);
	
	void setExternalDescription(String externalDescription);
	
	void setBaggingDate(String baggingDate);
	
	void setBaggingDate(int year, int month, int day);
	
	void setBaggingDate(Date date);
	
	void setExternalIdentifier(String externalIdentifier);
	
	void setBagSize(String bagSize);
	
	void generateBagSize(Bag bag);
	
	void setPayloadOxum(String payloadOxum);
	
	void setPayloadOxum(long octetCount, long streamCount);
	
	void generatePayloadOxum(Bag bag);
	
	void setBagGroupIdentifier(String bagGroupIdentifier);
	
	void setBagCount(String bagCount);
	
	void setBagCount(int bagInGroup, int totalBagsInGroup);
	
	void setInternalSenderIdentifier(String internalSenderIdentifier);
	
	void setInternalSenderDescription(String internalSenderDescription);
	
	String getSourceOrganization();
	
	String getOrganizationAddress();
	
	String getContactName();
	
	String getContactPhone();
	
	String getContactEmail();
	
	String getExternalDescription();
	
	String getBaggingDate();
	
	Date getBaggingDateObj() throws ParseException;
	
	String getExternalIdentifier();
	
	String getBagSize();
	
	String getPayloadOxum();
	
	Long getOctetCount() throws ParseException;
	
	Long getStreamCount() throws ParseException;
	
	String getBagGroupIdentifier();
	
	String getBagCount();
	
	Integer getBagInGroup() throws ParseException;
	
	Integer getTotalBagsInGroup() throws ParseException;
	
	String getInternalSenderIdentifier();
	
	String getInternalSenderDescription();
	
}
