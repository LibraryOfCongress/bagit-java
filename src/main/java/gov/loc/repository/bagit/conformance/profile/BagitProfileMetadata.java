package gov.loc.repository.bagit.conformance.profile;

import java.util.Objects;

public class BagitProfileMetadata {
  private String bagitProfileIdentifier;
  private String sourceOrganization;
  private String externalDescription;
  private String contactName;
  private String contactEmail;
  private String version;
  
  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof BagitProfileMetadata)) {
      return false;
    }
    final BagitProfileMetadata castOther = (BagitProfileMetadata) other;
    return Objects.equals(bagitProfileIdentifier, castOther.bagitProfileIdentifier)
        && Objects.equals(sourceOrganization, castOther.sourceOrganization)
        && Objects.equals(externalDescription, castOther.externalDescription)
        && Objects.equals(contactName, castOther.contactName) && Objects.equals(contactEmail, castOther.contactEmail)
        && Objects.equals(version, castOther.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bagitProfileIdentifier, sourceOrganization, externalDescription, contactName, contactEmail,
        version);
  }

  @Override
  public String toString() {
    return "BagitProfileInfo [bagitProfileIdentifier=" + bagitProfileIdentifier + ", sourceOrganization="
        + sourceOrganization + ", externalDescription=" + externalDescription + ", contactName=" + contactName
        + ", contactEmail=" + contactEmail + ", version=" + version + "]";
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

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }
}
