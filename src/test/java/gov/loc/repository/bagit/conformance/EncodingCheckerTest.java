package gov.loc.repository.bagit.conformance;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncodingCheckerTest {

  @Test
  public void testLinterCheckTagFilesEncoding(){
    Set<BagitWarning> warnings = new HashSet<>();
    EncodingChecker.checkEncoding(StandardCharsets.UTF_16, warnings, Collections.emptySet());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
  
  @Test
  public void testLinterIgnoreTagFilesEncoding(){
    Set<BagitWarning> warnings = new HashSet<>();
    EncodingChecker.checkEncoding(StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.TAG_FILES_ENCODING));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
}
