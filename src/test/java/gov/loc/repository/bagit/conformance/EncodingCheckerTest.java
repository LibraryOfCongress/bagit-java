package gov.loc.repository.bagit.conformance;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class EncodingCheckerTest extends Assert {

  @Test
  public void testLinterCheckTagFilesEncoding(){
    Set<BagitWarning> warnings = new HashSet<>();
    EncodingChecker.checkEncoding(StandardCharsets.UTF_16, warnings, Collections.emptySet());
    
    assertTrue(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
  
  @Test
  public void testLinterIgnoreTagFilesEncoding(){
    Set<BagitWarning> warnings = new HashSet<>();
    EncodingChecker.checkEncoding(StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.TAG_FILES_ENCODING));
    
    assertFalse(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
}
