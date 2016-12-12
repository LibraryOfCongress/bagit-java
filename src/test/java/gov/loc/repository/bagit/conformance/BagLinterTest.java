package gov.loc.repository.bagit.conformance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.verify.MySupportedNameToAlgorithmMapping;

public class BagLinterTest extends Assert{
  static { //add support for sha3
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
  
  private BagLinter sut;
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Before
  public void setup(){
    MySupportedNameToAlgorithmMapping mapping = new MySupportedNameToAlgorithmMapping();
    sut = new BagLinter(mapping);
  }
  
  @Test
  public void testLintBag() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Collections.emptyList());

    assertEquals(8, warnings.size()); //TODO add OS specific files
  }
  
  @Test
  public void testLinterIgnoreBagWithinABag() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.BAG_WITHIN_A_BAG));
    
    assertFalse(warnings.contains(BagitWarning.BAG_WITHIN_A_BAG));
  }
  
  @Test
  public void testLinterIgnoreWithRelativePaths() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.LEADING_DOT_SLASH));
    
    assertFalse(warnings.contains(BagitWarning.LEADING_DOT_SLASH));
  }
  
  @Test
  public void testLinterIgnorePayloadOxum() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.PAYLOAD_OXUM_MISSING));
    
    assertFalse(warnings.contains(BagitWarning.PAYLOAD_OXUM_MISSING));
  }
  
  @Test
  public void testLinterIgnoreWeakChecksum() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.WEAK_CHECKSUM_ALGORITHM));
    
    assertFalse(warnings.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM));
  }
  
  @Test
  public void testLinterIgnoreNonStandardChecksumAlgorithm() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.NON_STANDARD_ALGORITHM));
    
    assertFalse(warnings.contains(BagitWarning.NON_STANDARD_ALGORITHM));
  }
  
  @Test
  public void testLinterIgnoreTagFilesEncoding() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.TAG_FILES_ENCODING));
    
    assertFalse(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
  
  @Test
  public void testLinterIgnoreOldVersion() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.OLD_BAGIT_VERSION));
    
    assertFalse(warnings.contains(BagitWarning.OLD_BAGIT_VERSION));
  }
  
  @Test
  public void testLinterIgnoreOSSpecificFiles() throws Exception{
    Set<BagitWarning> warnings = sut.lintBag(rootDir, Arrays.asList(BagitWarning.OS_SPECIFIC_FILES));
    
    assertFalse(warnings.contains(BagitWarning.OS_SPECIFIC_FILES));
  }
}