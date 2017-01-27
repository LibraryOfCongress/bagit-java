package gov.loc.repository.bagit.conformance.profile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BagitProfile {
  @JsonProperty("BagIt-Profile-Info")
  private BagitProfileInfo bagitProfileInfo;
  
  @JsonProperty("Bag-Info")
  private BagInfo bagInfo;
  
  @JsonProperty("Manifests-Required")
  private List<String> manifestsRequired;
  
  @JsonProperty("Allow-Fetch.txt")
  private boolean allowFetchFile;
  
  @JsonProperty("Serialization")
  private Serialization serialization;
  
  @JsonProperty("Accept-Serialization")
  private List<String> acceptSerialization;
  
  @JsonProperty("Accept-BagIt-Version")
  private List<String> acceptBagitVersion;
  
  @JsonProperty("Tag-Manifests-Required")
  private List<String> tagManifestsRequired;
  
  @JsonProperty("Tag-Files-Required")
  private List<String> tagFilesRequired;
  
  public BagitProfileInfo getBagitProfileInfo() {
    return bagitProfileInfo;
  }
  public void setBagitProfileInfo(BagitProfileInfo bagitProfileInfo) {
    this.bagitProfileInfo = bagitProfileInfo;
  }
  public BagInfo getBagInfo() {
    return bagInfo;
  }
  public void setBagInfo(BagInfo bagInfo) {
    this.bagInfo = bagInfo;
  }
  public List<String> getManifestsRequired() {
    return manifestsRequired;
  }
  public void setManifestsRequired(List<String> manifestsRequired) {
    this.manifestsRequired = manifestsRequired;
  }
  public boolean isAllowFetchFile() {
    return allowFetchFile;
  }
  public void setAllowFetchFile(boolean allowFetchFile) {
    this.allowFetchFile = allowFetchFile;
  }
  public Serialization getSerialization() {
    return serialization;
  }
  public void setSerialization(Serialization serialization) {
    this.serialization = serialization;
  }
  public List<String> getAcceptSerialization() {
    return acceptSerialization;
  }
  public void setAcceptSerialization(List<String> acceptSerialization) {
    this.acceptSerialization = acceptSerialization;
  }
  public List<String> getAcceptBagitVersion() {
    return acceptBagitVersion;
  }
  public void setAcceptBagitVersion(List<String> acceptBagitVersion) {
    this.acceptBagitVersion = acceptBagitVersion;
  }
  public List<String> getTagManifestsRequired() {
    return tagManifestsRequired;
  }
  public void setTagManifestsRequired(List<String> tagManifestsRequired) {
    this.tagManifestsRequired = tagManifestsRequired;
  }
  public List<String> getTagFilesRequired() {
    return tagFilesRequired;
  }
  public void setTagFilesRequired(List<String> tagFilesRequired) {
    this.tagFilesRequired = tagFilesRequired;
  }
}
