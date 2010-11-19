package gov.loc.repository.bagit;

import gov.loc.repository.bagit.utilities.namevalue.NameValueMapList;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/*
 * A bag-info.txt file containing name-value pairs.
 * 
 * According to the BagIt Spec, the bag-info.txt is a set of name-value pairs.
 * It does not specify whether ordering is significant, capitalization is
 * significant, or if fields can be repeated.
 * 
 * Prior to version 3.7, this implementation treated ordering as significant and
 * capitalization as insignificant, but did not allow fields to be repeated.
 * Not allowing fields to be repeated if reflected in the use of the Map interface.
 * 
 * Starting with version 3.7, this implementation supports repeated fields, but still maintains
 * the Map interface.  For getters which only return a single value for a field, if there
 * are multiple instances of the field, only the first value is set.  For setters in the
 * Map interface or setters for standard fields, the new value replaces the value of the first instance. 
 */
public interface BagInfoTxt extends BagFile, NameValueMapList {
	
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
	
	List<String> getStandardFields();
	
	List<String> getNonstandardFields();

	List<String> getListCaseInsensitive(String key);
	
	String getCaseInsensitive(String key);
	
	boolean containsKeyCaseInsensitive(String key);

}
