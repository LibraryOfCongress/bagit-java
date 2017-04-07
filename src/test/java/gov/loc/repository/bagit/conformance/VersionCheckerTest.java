package gov.loc.repository.bagit.conformance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Version;

public class VersionCheckerTest extends Assert {

  @Test
  public void testCheckOldVersion(){
    Set<BagitWarning> warnings = new HashSet<>();
    VersionChecker.checkVersion(new Version(0, 95), warnings, Collections.emptyList());
    
    assertTrue(warnings.contains(BagitWarning.OLD_BAGIT_VERSION));
  }
  
  @Test
  public void testLinterIgnoreOldVersion(){
    Set<BagitWarning> warnings = new HashSet<>();
    VersionChecker.checkVersion(new Version(0, 95), warnings, Arrays.asList(BagitWarning.OLD_BAGIT_VERSION));
    
    assertFalse(warnings.contains(BagitWarning.OLD_BAGIT_VERSION));
  }
}
