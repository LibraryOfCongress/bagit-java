package gov.loc.repository.bagit.conformance.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BagitProfileInfo {
  @JsonProperty("BagIt-Profile-Identifier")
  private String bagitProfileIdentifier;
  
  @JsonProperty("Source-Organization")
  private String sourceOrganization;
  
  @JsonProperty("External-Description")
  private String externalDescription;
  
  @JsonProperty("Contact-Name")
  private String contactName;
  
  @JsonProperty("Contact-Email")
  private String contactEmail;
  
  @JsonProperty("Version")
  private String version;

  public String getBagitProfileIdentifier() {
    return bagitProfileIdentifier;
  }

  public void setBagitProfileIdentifier(String bagitProfileIdentifier) {
    this.bagitProfileIdentifier = bagitProfileIdentifier;
  }

  public String getSourceOrganization() {
    return sourceOrganization;
  }

  public void setSourceOrganization(String sourceOrganization) {
    this.sourceOrganization = sourceOrganization;
  }

  public String getExternalDescription() {
    return externalDescription;
  }

  public void setExternalDescription(String externalDescription) {
    this.externalDescription = externalDescription;
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
