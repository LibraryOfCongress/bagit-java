package gov.loc.repository.bagit.conformance;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;

public class MetadataCheckerTest extends PrivateConstructorTest {
  
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MetadataChecker.class);
  }

  @Test
  public void testLinterCheckForPayloadOxum() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();
    MetadataChecker.checkBagMetadata(rootDir, StandardCharsets.UTF_16, warnings, Collections.emptySet());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.PAYLOAD_OXUM_MISSING));
  }
  
  @Test
  public void testLinterIgnorePayloadOxum() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();
    MetadataChecker.checkBagMetadata(rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.PAYLOAD_OXUM_MISSING));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.PAYLOAD_OXUM_MISSING));
  }
}
