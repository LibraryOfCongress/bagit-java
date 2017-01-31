package gov.loc.repository.bagit.conformance.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * POJO for all the bagit profile fields
 */
public class BagitProfile {
  private BagitProfileMetadata bagitProfileMetadata = new BagitProfileMetadata();
  private Map<String, BagInfoEntry> bagInfoEntryRequirements = new HashMap<>();
  private List<String> manifestTypesRequired = new ArrayList<>();
  private boolean fetchFileAllowed; //defaults to false
  private Serialization serialization = Serialization.optional;
  private List<String> acceptableMIMESerializationTypes = new ArrayList<>();
  private List<String> acceptableBagitVersions = new ArrayList<>();
  private List<String> tagManifestsRequired = new ArrayList<>();
  private List<String> tagFilesRequired = new ArrayList<>();
  
  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof BagitProfile)) {
      return false;
    }
    final BagitProfile castOther = (BagitProfile) other;
    return Objects.equals(bagitProfileMetadata, castOther.bagitProfileMetadata) && Objects.equals(bagInfoEntryRequirements, castOther.bagInfoEntryRequirements)
        && Objects.equals(manifestTypesRequired, castOther.manifestTypesRequired)
        && Objects.equals(fetchFileAllowed, castOther.fetchFileAllowed)
        && Objects.equals(serialization, castOther.serialization)
        && Objects.equals(acceptableMIMESerializationTypes, castOther.acceptableMIMESerializationTypes)
        && Objects.equals(acceptableBagitVersions, castOther.acceptableBagitVersions)
        && Objects.equals(tagManifestsRequired, castOther.tagManifestsRequired)
        && Objects.equals(tagFilesRequired, castOther.tagFilesRequired);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bagitProfileMetadata, bagInfoEntryRequirements, manifestTypesRequired, fetchFileAllowed, serialization,
        acceptableMIMESerializationTypes, acceptableBagitVersions, tagManifestsRequired, tagFilesRequired);
  }

  @Override
  public String toString() {
    return "BagitProfile [bagitProfileInfo=" + bagitProfileMetadata + ", bagInfo=" + bagInfoEntryRequirements + ", manifestsRequired="
        + manifestTypesRequired + ", allowFetchFile=" + fetchFileAllowed + ", serialization=" + serialization
        + ", acceptSerialization=" + acceptableMIMESerializationTypes + ", acceptBagitVersion=" + acceptableBagitVersions
        + ", tagManifestsRequired=" + tagManifestsRequired + ", tagFilesRequired=" + tagFilesRequired + "]";
  }
  
  public BagitProfileMetadata getBagitProfileMetadata() {
    return bagitProfileMetadata;
  }
  public void setBagitProfileMetadata(final BagitProfileMetadata bagitProfileInfo) {
    this.bagitProfileMetadata = bagitProfileInfo;
  }
  public Map<String,BagInfoEntry> getBagInfoEntryRequirements() {
    return bagInfoEntryRequirements;
  }
  public void setBagInfoEntryRequirements(final Map<String, BagInfoEntry> bagInfo) {
    this.bagInfoEntryRequirements = bagInfo;
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
  public List<String> getTagManifestsRequired() {
    return tagManifestsRequired;
  }
  public void setTagManifestsRequired(final List<String> tagManifestsRequired) {
    this.tagManifestsRequired = tagManifestsRequired;
  }
  public List<String> getTagFilesRequired() {
    return tagFilesRequired;
  }
  public void setTagFilesRequired(final List<String> tagFilesRequired) {
    this.tagFilesRequired = tagFilesRequired;
  }
}
