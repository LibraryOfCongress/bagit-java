package gov.loc.repository.bagit.conformance.profile;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class BagInfoRequirementTest extends Assert {

  @Test
  public void testEquals(){
    BagInfoRequirement requirement = new BagInfoRequirement(true, Arrays.asList("foo"));
    BagInfoRequirement sameRequirement = new BagInfoRequirement(true, Arrays.asList("foo"));
    assertEquals(requirement, sameRequirement);
    
    assertFalse(requirement.equals(null));
    
    BagInfoRequirement differentRequirement = new BagInfoRequirement(false, Arrays.asList("foo"));
    assertFalse(requirement.equals(differentRequirement));
    
    BagInfoRequirement differentListOfAcceptableValues = new BagInfoRequirement();
    differentListOfAcceptableValues.setRequired(true);
    differentListOfAcceptableValues.setAcceptableValues(Arrays.asList("bar"));
    assertFalse(requirement.equals(differentListOfAcceptableValues));
  }
}
