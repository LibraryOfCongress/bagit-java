package gov.loc.repository.bagit.conformance.profile;

import java.util.List;

public class BagInfo {
  private List<BagInfoEntry> requirements;

  public List<BagInfoEntry> getRequirements() {
    return requirements;
  }

  public void setRequirements(List<BagInfoEntry> requirements) {
    this.requirements = requirements;
  }
}
