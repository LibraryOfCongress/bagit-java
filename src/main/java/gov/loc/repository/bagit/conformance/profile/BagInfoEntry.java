package gov.loc.repository.bagit.conformance.profile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BagInfoEntry {

  @JsonProperty
  private String name;
  
  @JsonProperty
  private boolean required;
  
  @JsonProperty
  private List<String> acceptableValues;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public boolean isRequired() {
    return required;
  }
  public void setRequired(boolean required) {
    this.required = required;
  }
  public List<String> getAcceptableValues() {
    return acceptableValues;
  }
  public void setAcceptableValues(List<String> acceptableValues) {
    this.acceptableValues = acceptableValues;
  }
}
