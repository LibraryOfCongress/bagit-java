package gov.loc.repository.bagit.verify;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.TempFolderTest;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.exceptions.VerificationException;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.reader.BagReader;

public class BagVerifierTest extends TempFolderTest{
  static {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  
  private BagVerifier sut = new BagVerifier();
  private BagReader reader = new BagReader();
  
  @Test
  public void testStandardSupportedAlgorithms() throws Exception{
    List<String> algorithms = Arrays.asList("md5", "sha1", "sha256", "sha512");
    for(String alg : algorithms){
      StandardSupportedAlgorithms algorithm = StandardSupportedAlgorithms.valueOf(alg.toUpperCase());
      Manifest manifest = new Manifest(algorithm);
      sut.checkHashes(manifest);
    }
  }
  
  @Test
  public void testVersion0_97IsValid() throws Exception{
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testVersion2_0IsValid() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bags/v2_0/bag").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testIsComplete() throws Exception{
    Bag bag = reader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test
  public void testCorruptPayloadFile() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/corruptPayloadFile").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(CorruptChecksumException.class, () -> { sut.isValid(bag, true); });
  }
  
  @Test
  public void testCorruptTagFile() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/corruptTagFile").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(CorruptChecksumException.class, () -> { sut.isValid(bag, true); });
  }
  
  @Test
  public void testErrorWhenUnspportedAlgorithmException() throws Exception{
    Path sha3BagDir = Paths.get(getClass().getClassLoader().getResource("sha3Bag").toURI());
    MySupportedNameToAlgorithmMapping mapping = new MySupportedNameToAlgorithmMapping();
    BagReader extendedReader = new BagReader(mapping);
    Bag bag = extendedReader.read(sha3BagDir);
    
    Assertions.assertThrows(UnsupportedAlgorithmException.class, () -> { sut.isValid(bag, true); });
  }
  
  @Test
  public void testVerificationExceptionIsThrownForNoSuchAlgorithmException() throws Exception{
    Path unreadableFile = createFile("newFile");
    
    Manifest manifest = new Manifest(new SupportedAlgorithm() {
      @Override
      public String getMessageDigestName() {
        return "FOO";
      }
      @Override
      public String getBagitName() {
        return "foo";
      }
    });
    manifest.getFileToChecksumMap().put(unreadableFile, "foo");
    
    Assertions.assertThrows(VerificationException.class, () -> { sut.checkHashes(manifest); });
  }
  
  @Test
  public void testAddSHA3SupportViaExtension() throws Exception{
    Path sha3BagDir = Paths.get(new File("src/test/resources/sha3Bag").toURI());
    MySupportedNameToAlgorithmMapping mapping = new MySupportedNameToAlgorithmMapping();
    BagReader extendedReader = new BagReader(mapping);
    Bag bag = extendedReader.read(sha3BagDir);
    try(BagVerifier extendedSut = new BagVerifier(mapping)){
      extendedSut.isValid(bag, true);
    }
  }
  
  /*
   * Technically valid but highly discouraged
   */
  @Test
  public void testManifestsWithLeadingDotSlash() throws Exception{
    Path bagPath = Paths.get(new File("src/test/resources/bag-with-leading-dot-slash-in-manifest").toURI());
    Bag bag = reader.read(bagPath);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testCanQuickVerify() throws Exception{
    Bag bag = reader.read(rootDir);
    boolean canQuickVerify = BagVerifier.canQuickVerify(bag);
    Assertions.assertFalse(canQuickVerify,
        "Since " + bag.getRootDir() + " DOES NOT contain the metadata Payload-Oxum then it should return false!");
    
    Path passingRootDir = Paths.get(new File("src/test/resources/bags/v0_94/bag").toURI());
    bag = reader.read(passingRootDir);
    canQuickVerify = BagVerifier.canQuickVerify(bag);
    Assertions.assertTrue(canQuickVerify,
        "Since " + bag.getRootDir() + " DOES contain the metadata Payload-Oxum then it should return true!");
  }
  
  @Test 
  public void testQuickVerify() throws Exception{
    Path passingRootDir = Paths.get(new File("src/test/resources/bags/v0_94/bag").toURI());
    Bag bag = reader.read(passingRootDir);
    
    BagVerifier.quicklyVerify(bag);
  }
}
