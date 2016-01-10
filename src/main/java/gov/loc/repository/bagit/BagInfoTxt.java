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
	
	/**
	 * Sets the Source Organization metadata field (No abbreviations).
	 * @param sourceOrganization The organization where the bag was made.
	 */
	void setSourceOrganization(String sourceOrganization);
	
	/**
	 * Adds the Source Organization metadata field (No abbreviations).
	 * @param sourceOrganization The organization where the bag was made.
	 */
	void addSourceOrganization(String sourceOrganization);
	
	/**
	 * Sets the Source Organization Address metadata field.
	 * @param organizationAddress The address of the Source Organization.
	 */
	void setOrganizationAddress(String organizationAddress);
	
	/**
	 * Adds the Source Organization Address metadata field.
	 * @param organizationAddress The address of the Source Organization.
	 */
	void addOrganizationAddress(String organizationAddress);
	
	/**
	 * Sets the Contact Name metadata field.
	 * @param contactName The name of the person responsible for the bag.
	 */
	void setContactName(String contactName);
	
	/**
	 * Adds the Contact Name metadata field.
	 * @param contactName The name of the person responsible for the bag.
	 */
	void addContactName(String contactName);
	
	/**
	 * Sets the Contact Phone metadata field.
	 * @param contactPhone Phone number of the person from Contact Name.
	 */
	void setContactPhone(String contactPhone);
	
	/**
	 * Adds the Contact Phone metadata field.
	 * @param contactPhone Phone number of the person from Contact Name.
	 */
	void addContactPhone(String contactPhone);
	
	/**
	 * Sets the Contact Email metadata field.
	 * @param contactEmail Email address of the person in Contact Name.
	 */
	void setContactEmail(String contactEmail);
	
	/**
	 * Adds the Contact Email metadata field.
	 * @param contactEmail Email address of the person in Contact Name.
	 */
	void addContactEmail(String contactEmail);
	
	/**
	 * Sets the External Description metadata field.
	 * @param externalDescription A description of the bag's contents
	 * for those outside of your organization.
	 */
	void setExternalDescription(String externalDescription);
	
	/**
	 * Adds the External Description metadata field.
	 * @param externalDescription A description of the bag's contents
	 * for those outside of your organization.
	 */
	void addExternalDescription(String externalDescription);
	
	/**
	 * Sets the Bagging Date metadata field using a String format.
	 * @param baggingDate The date the bag was created.
	 */
	void setBaggingDate(String baggingDate);
	
	/**
	 * Sets the Bagging Date metadata field using Integers to populate the Date.
	 * @param year The year the bag was created.
	 * @param month The month the bag was created.
	 * @param day The day the bag was created.
	 */
	void setBaggingDate(int year, int month, int day);
	
	/**
	 * Sets the Bagging Date metadata field using a Date format.
	 * @param date The date the bag was created.
	 */
	void setBaggingDate(Date date);
	
	/**
	 * Sets the External Identifier metadata field.
	 * @param externalIdentifier A sender-supplied identifier for the bag.
	 */
	void setExternalIdentifier(String externalIdentifier);
	
	/**
	 * Adds the External Identifier metadata field.
	 * @param externalIdentifier A sender-supplied identifier for the bag.
	 */
	void addExternalIdentifier(String externalIdentifier);
	
	/**
	 * Sets the Bag Size metadata field, usually created by the bagging tool.
	 * @param bagSize The size of the bag.
	 */
	void setBagSize(String bagSize);
	
	/**
	 * Generates the Bag Size metadata field from a given bag.
	 * @param bag The bag to generate it's Bag Size.
	 */
	void generateBagSize(Bag bag);
	
	/**
	 * Sets the Payload Oxum metadata field, usually created by the bagging tool.
	 * @param payloadOxum A byte count and file count signature for the bag.
	 */
	void setPayloadOxum(String payloadOxum);
	
	/**
	 * Sets the Payload Oxum metadata field with supplied parameters.
	 * @param octetCount A byte count for the bag.
	 * @param streamCount A file count signature for the bag.
	 */
	void setPayloadOxum(long octetCount, long streamCount);
	
	/**
	 * Generates the Payload Oxum metadata field from a given bag.
	 * @param bag The bag to generate it's Payload Oxum.
	 */
	void generatePayloadOxum(Bag bag);
	
	/**
	 * Sets the Bag Group Identifier metadata field.
	 * @param bagGroupIdentifier A unique name given to a group of more than one bag.
	 */
	void setBagGroupIdentifier(String bagGroupIdentifier);
	
	/**
	 * Adds the Bag Group Identifier metadata field.
	 * @param bagGroupIdentifier A unique name given to a group of more than one bag.
	 */
	void addBagGroupIdentifier(String bagGroupIdentifier);
	
	/**
	 * Sets the Bag Count metadata field.
	 * @param bagCount This bag's sequence number, if part of a group of bags.
	 */
	void setBagCount(String bagCount);
	
	/**
	 * Sets the Bag Count metadata field with supplied parameters.
	 * @param bagInGroup This bag's sequence number.
	 * @param totalBagsInGroup Total amount of bags, if part of a group of bags.
	 */
	void setBagCount(int bagInGroup, int totalBagsInGroup);
	
	/**
	 * Sets the Internal Sender Identifier metadata field.
	 * @param internalSenderIdentifier The ID assigned to this content
	 * internally at your institution, if any.
	 */
	void setInternalSenderIdentifier(String internalSenderIdentifier);
	
	/**
	 * Adds the Internal Sender Identifier metadata field.
	 * @param internalSenderIdentifier The ID assigned to this content
	 * internally at your institution, if any.
	 */
	void addInternalSenderIdentifier(String internalSenderIdentifier);
	
	/**
	 * Sets the Internal Sender Description metadata field.
	 * @param internalSenderDescription A written description of the contents
	 * of the bag based on internal standards.
	 */
	void setInternalSenderDescription(String internalSenderDescription);
	
	/**
	 * Adds the Internal Sender Description metadata field.
	 * @param internalSenderDescription A written description of the contents
	 * of the bag based on internal standards.
	 */
	void addInternalSenderDescription(String internalSenderDescription);
	
	/**
	 * Gets the Source Organization of this bag.
	 * @return The organization where the bag was made.
	 */
	String getSourceOrganization();
	
	/**
	 * Gets a list of the Source Organizations belonging to this bag.
	 * @return A list of organizations where the bag was made.
	 */
	List<String> getSourceOrganizationList();
	
	/**
	 * Gets the Source Organization Address of this bag.
	 * @return The address of the Source Organization.
	 */
	String getOrganizationAddress();
	
	/**
	 * Gets a list of Source Organization Addresses belonging to this bag.
	 * @return A list of addresses belonging to the Source Organizations.
	 */
	List<String> getOrganizationAddressList();
	
	/**
	 * Gets the Contact Name of this bag.
	 * @return The name of the person responsible for the bag.
	 */
	String getContactName();
	
	/**
	 * Gets a list of Contact Names belonging to this bag.
	 * @return A list of the names of the people responsible for the bag.
	 */
	List<String> getContactNameList();
	
	/**
	 * Gets the Contact Phone number of this bag.
	 * @return The phone number of the person from the Contact Name.
	 */
	String getContactPhone();
	
	/**
	 * Gets a list of Contact Phone numbers belonging to this bag.
	 * @return A list of phone numbers of the people from the Contact Name.
	 */
	List<String> getContactPhoneList();
	
	/**
	 * Gets the Contact Email of this bag.
	 * @return The email address of the person in Contact Name.
	 */
	String getContactEmail();
	
	/**
	 * Gets a list of Contact Email's belonging to this bag.
	 * @return A list of email addresses of the people from the Contact Name.
	 */
	List<String> getContactEmailList();
	
	/**
	 * Gets the External Description of this bag.
	 * @return A description of the bag's contents.
	 */
	String getExternalDescription();
	
	/**
	 * Gets a list of External Descriptions belonging to this bag.
	 * @return A list of the descriptions of the bag's contents.
	 */
	List<String> getExternalDescriptionList();
	
	/**
	 * Gets the Bagging Date of this bag.
	 * @return The date the bag was created.
	 */
	String getBaggingDate();
	
	/**
	 * Gets a Date object from the Bagging Date metadata field.
	 * @return The date the bag was created in a Date format.
	 * @throws ParseException Throws an exception when unable to parse
	 * a Date object from the Bagging Date field.
	 */
	Date getBaggingDateObj() throws ParseException;
	
	/**
	 * Gets the External Identifier of this bag.
	 * @return A sender-supplied identifier for the bag.
	 */
	String getExternalIdentifier();
	
	/**
	 * Gets a list of External Identifiers belonging to this bag.
	 * @return A list of the sender-supplied identifiers for the bag.
	 */
	List<String> getExternalIdentifierList();
	
	/**
	 * Gets the Bag Size of this bag.
	 * @return The size of the bag.
	 */
	String getBagSize();
	
	/**
	 * Gets the Payload Oxum of this bag.
	 * @return The byte count and file count signature for the bag.
	 */
	String getPayloadOxum();
	
	/**
	 * Gets the byte count of this bag as a Long object.
	 * @return The byte count as a Long object.
	 * @throws ParseException Throws an exception when unable to parse
	 * a Long object from the OctetCount field of the Payload Oxum.
	 */
	Long getOctetCount() throws ParseException;
	
	/**
	 * Gets the file count signature as a Long object.
	 * @return The file count signature as a Long object.
	 * @throws ParseException Throws an exception when unable to parse
	 * a Long object from the StreamCount field of the Payload Oxum.
	 */
	Long getStreamCount() throws ParseException;
	
	/**
	 * Gets the Bag Group Identifier of this bag.
	 * @return A unique name given to a group of more than one bag.
	 */
	String getBagGroupIdentifier();
	
	/**
	 * Gets a list of Bag Group Identifiers belonging to this bag.
	 * @return A list of unique names given to a group of more than one bag.
	 */
	List<String> getBagGroupIdentifierList();
	
	/**
	 * Gets the Bag Count of this bag, if part of a group of bags.
	 * @return This bag's sequencing number.
	 */
	String getBagCount();
	
	/**
	 * Gets the Bag Count of this bag, if part of a group of bags,
	 * as a Long object.
	 * @return The Bag Count of this bag as a Long object.
	 * @throws ParseException Throws an exception when unable to parse
	 * a Long object from the Bag Count.
	 */
	Integer getBagInGroup() throws ParseException;
	
	/**
	 * Gets the total number of Bags, if part of a group of bags,
	 * as a Long object.
	 * @return The total number of bags as a Long object.
	 * @throws ParseException Throws an exception when unable to parse
	 * a Long object from the total number of bags.
	 */
	Integer getTotalBagsInGroup() throws ParseException;
	
	/**
	 * Gets the Internal Sender Identifier of this bag.
	 * @return The ID assigned to this content internally
	 * at your institution, if any.
	 */
	String getInternalSenderIdentifier();
	
	/**
	 * Gets a list of Internal Sender Identifiers of this bag.
	 * @return  A list of ID's assigned to this content internally
	 * at your institution, if any.
	 */
	List<String> getInternalSenderIdentifierList();
	
	/**
	 * Gets the Internal Sender Description of this bag.
	 * @return A written description of the contents of the bag
	 * based on internal standards.
	 */
	String getInternalSenderDescription();
	
	/**
	 * Gets a list of Internal Sender Descriptions of this bag.
	 * @return A list of written descriptions of the contents of the bag
	 * based on internal standards.
	 */
	List<String> getInternalSenderDescriptionList();
	
	/**
	 * Gets a list of the Standard metadata fields of this bag.
	 * @return A list of Standard metadata fields belonging to this bag.
	 */
	List<String> getStandardFields();
	
	/**
	 * Gets a list of the Non-Standard metadata fields of this bag.
	 * @return A list of Non-Standard metadata fields belonging to this bag.
	 */
	List<String> getNonstandardFields();

	/**
	 * Gets a list of the metadata fields that are case-insensitive.
	 * @param key A String that describes which metadata field to retrieve.
	 * @return A list of the metadata fields.
	 */
	List<String> getListCaseInsensitive(String key);
	
	/**
	 * Gets a Case Insensitive metadata field.
	 * @param key A String that describes which metadata field to retrieve.
	 * @return The metadata field belonging to the key.
	 */
	String getCaseInsensitive(String key);
	
	/**
	 * Checks whether or not the bag-info tag files contains a
	 * case-insensitive field.
	 * @param key A String that describes which metadata field to retrieve.
	 * @return True if the bag-info tag files contains the case-insensitive
	 * metadata field, false otherwise.
	 */
	boolean containsKeyCaseInsensitive(String key);

}