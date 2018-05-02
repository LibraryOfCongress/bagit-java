package gov.loc.repository.bagit.conformance.profile;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BagInfoRequirementTest {

  @Test
  public void testEquals(){
    BagInfoRequirement requirement = new BagInfoRequirement(true, Arrays.asList("foo"));
    BagInfoRequirement sameRequirement = new BagInfoRequirement(true, Arrays.asList("foo"));
    Assertions.assertEquals(requirement, sameRequirement);
    
    Assertions. assertFalse(requirement.equals(null));
    
    BagInfoRequirement differentRequirement = new BagInfoRequirement(false, Arrays.asList("foo"));
    Assertions.assertFalse(requirement.equals(differentRequirement));
    
    BagInfoRequirement differentListOfAcceptableValues = new BagInfoRequirement();
    differentListOfAcceptableValues.setRequired(true);
    differentListOfAcceptableValues.setAcceptableValues(Arrays.asList("bar"));
    differentListOfAcceptableValues.setRepeatable(false);
    Assertions.assertFalse(requirement.equals(differentListOfAcceptableValues));
  }
}
