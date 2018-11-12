package gov.loc.repository.bagit.conformance.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to define elements in a bag-info.txt file used by a bagit-profile.
 */
public class BagInfoRequirement {
  private boolean required = false;
  private List<String> acceptableValues = new ArrayList<>();
  private boolean repeatable = true;
  
  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof BagInfoRequirement)) {
      return false;
    }
    final BagInfoRequirement castOther = (BagInfoRequirement) other;
    return Objects.equals(required, castOther.required)
        && Objects.equals(acceptableValues, castOther.acceptableValues)
        && Objects.equals(repeatable, castOther.repeatable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(required, acceptableValues, repeatable);
  }

  public BagInfoRequirement(){
    //intentionally left empty
  }
  
  public BagInfoRequirement(final boolean required, final List<String> acceptableValues){
    this.required = required;
    this.acceptableValues = acceptableValues;
  }
  
  public BagInfoRequirement(final boolean required, final List<String> acceptableValues, boolean repeatable){
    this.required = required;
    this.acceptableValues = acceptableValues;
    this.repeatable = repeatable;
  }
  
  @Override
  public String toString() {
    return "[required=" + required + ", acceptableValues=" + acceptableValues + ", repeatable=" + repeatable + "]";
  }
  
  public boolean isRequired() {
    return required;
  }
  public void setRequired(final boolean required) {
    this.required = required;
  }
  public List<String> getAcceptableValues() {
    return acceptableValues;
  }
  public void setAcceptableValues(final List<String> acceptableValues) {
    this.acceptableValues = acceptableValues;
  }
  public boolean isRepeatable() {
    return repeatable;
  }
  public void setRepeatable(final boolean repeatable) {
    this.repeatable = repeatable;
  }
}
