package gov.loc.repository.bagit.conformance.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * POJO for all the bagit profile fields. 
 * A bagit profile is used to ensure the bag metadata contains all required elements and optional elements follow allowed values
 */
public class BagitProfile {
  private String bagitProfileIdentifier = "";
  private String sourceOrganization = "";
  private String externalDescription = "";
  private String contactName = "";
  private String contactEmail = "";
  private String contactPhone = "";
  private String version = "";
  
  private Map<String, BagInfoRequirement> bagInfoRequirements = new HashMap<>();
  private List<String> manifestTypesRequired = new ArrayList<>();
  private boolean fetchFileAllowed; //defaults to false
  private Serialization serialization = Serialization.optional;
  private List<String> acceptableMIMESerializationTypes = new ArrayList<>();
  private List<String> acceptableBagitVersions = new ArrayList<>();
  private List<String> tagManifestTypesRequired = new ArrayList<>();
  private List<String> tagFilesRequired = new ArrayList<>();
  
  
  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof BagitProfile)) {
      return false;
    }
    final BagitProfile castOther = (BagitProfile) other;
    return Objects.equals(bagitProfileIdentifier, castOther.bagitProfileIdentifier)
        && Objects.equals(sourceOrganization, castOther.sourceOrganization)
        && Objects.equals(externalDescription, castOther.externalDescription)
        && Objects.equals(contactName, castOther.contactName) && Objects.equals(contactEmail, castOther.contactEmail)
        && Objects.equals(contactPhone, castOther.contactPhone)
        && Objects.equals(version, castOther.version)
        && Objects.equals(bagInfoRequirements, castOther.bagInfoRequirements)
        && Objects.equals(manifestTypesRequired, castOther.manifestTypesRequired)
        && Objects.equals(fetchFileAllowed, castOther.fetchFileAllowed)
        && Objects.equals(serialization, castOther.serialization)
        && Objects.equals(acceptableMIMESerializationTypes, castOther.acceptableMIMESerializationTypes)
        && Objects.equals(acceptableBagitVersions, castOther.acceptableBagitVersions)
        && Objects.equals(tagManifestTypesRequired, castOther.tagManifestTypesRequired)
        && Objects.equals(tagFilesRequired, castOther.tagFilesRequired);
  }
  @Override
  public int hashCode() {
    return Objects.hash(bagitProfileIdentifier, sourceOrganization, externalDescription, contactName, contactEmail, contactPhone, version, bagInfoRequirements, manifestTypesRequired, fetchFileAllowed, serialization,
        acceptableMIMESerializationTypes, acceptableBagitVersions, tagManifestTypesRequired, tagFilesRequired);
  }
  @Override
  public String toString() {
    return "BagitProfile [bagitProfileIdentifier=" + bagitProfileIdentifier + ", sourceOrganization="
        + sourceOrganization + ", externalDescription=" + externalDescription + ", contactName=" + contactName
        + ", contactEmail=" + contactEmail + ", contactPhone=" + contactPhone + ", version=" + version + ", bagInfoRequirements=" + bagInfoRequirements
        + ", manifestTypesRequired=" + manifestTypesRequired + ", fetchFileAllowed=" + fetchFileAllowed
        + ", serialization=" + serialization + ", acceptableMIMESerializationTypes=" + acceptableMIMESerializationTypes
        + ", acceptableBagitVersions=" + acceptableBagitVersions + ", tagManifestTypesRequired="
        + tagManifestTypesRequired + ", tagFilesRequired=" + tagFilesRequired + "]";
  }
  
  public Map<String,BagInfoRequirement> getBagInfoRequirements() {
    return bagInfoRequirements;
  }
  public void setBagInfoRequirements(final Map<String, BagInfoRequirement> bagInfo) {
    this.bagInfoRequirements = bagInfo;
  }
  public List<String> getManifestTypesRequired() {
    return manifestTypesRequired;
  }
  public void setManifestTypesRequired(final List<String> manifestsRequired) {
    this.manifestTypesRequired = manifestsRequired;
  }
  public boolean isFetchFileAllowed() {
    return fetchFileAllowed;
  }
  public void setFetchFileAllowed(final boolean allowFetchFile) {
    this.fetchFileAllowed = allowFetchFile;
  }
  public Serialization getSerialization() {
    return serialization;
  }
  public void setSerialization(final Serialization serialization) {
    this.serialization = serialization;
  }
  public List<String> getAcceptableMIMESerializationTypes() {
    return acceptableMIMESerializationTypes;
  }
  public void setAcceptableMIMESerializationTypes(final List<String> acceptSerialization) {
    this.acceptableMIMESerializationTypes = acceptSerialization;
  }
  public List<String> getAcceptableBagitVersions() {
    return acceptableBagitVersions;
  }
  public void setAcceptableBagitVersions(final List<String> acceptBagitVersion) {
    this.acceptableBagitVersions = acceptBagitVersion;
  }
  public List<String> getTagManifestTypesRequired() {
    return tagManifestTypesRequired;
  }
  public void setTagManifestTypesRequired(final List<String> tagManifestsRequired) {
    this.tagManifestTypesRequired = tagManifestsRequired;
  }
  public List<String> getTagFilesRequired() {
    return tagFilesRequired;
  }
  public void setTagFilesRequired(final List<String> tagFilesRequired) {
    this.tagFilesRequired = tagFilesRequired;
  }
  public String getBagitProfileIdentifier() {
    return bagitProfileIdentifier;
  }
  public void setBagitProfileIdentifier(final String bagitProfileIdentifier) {
    this.bagitProfileIdentifier = bagitProfileIdentifier;
  }
  public String getSourceOrganization() {
    return sourceOrganization;
  }
  public void setSourceOrganization(final String sourceOrganization) {
    this.sourceOrganization = sourceOrganization;
  }
  public String getExternalDescription() {
    return externalDescription;
  }
  public void setExternalDescription(final String externalDescription) {
    this.externalDescription = externalDescription;
  }
  public String getContactName() {
    return contactName;
  }
  public void setContactName(final String contactName) {
    this.contactName = contactName;
  }
  public String getContactEmail() {
    return contactEmail;
  }
  public void setContactEmail(final String contactEmail) {
    this.contactEmail = contactEmail;
  }
  public String getContactPhone() {
    return contactPhone;
  }
  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }
  public String getVersion() {
    return version;
  }
  public void setVersion(final String version) {
    this.version = version;
  }
}
