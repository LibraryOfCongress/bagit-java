package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInManifestException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidPayloadOxumException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.exceptions.PayloadOxumDoesNotExistException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.reader.BagReader;

public class BagVerifierTest extends Assert{
  static {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  
  private BagVerifier sut;
  
  @Before
  public void setup() throws NoSuchAlgorithmException{
    sut = new BagVerifier();
  }
  
  @Test
  public void testCanQuickVerify() throws Exception{
    Bag bag = BagReader.read(rootDir);
    boolean canQuickVerify = sut.canQuickVerify(bag);
    assertFalse("Since " + bag.getRootDir() + " DOES NOT contain the metadata Payload-Oxum then it should return false!", canQuickVerify);
    
    Path passingRootDir = Paths.get(new File("src/test/resources/bags/v0_94/bag").toURI());
    bag = BagReader.read(passingRootDir);
    canQuickVerify = sut.canQuickVerify(bag);
    assertTrue("Since " + bag.getRootDir() + " DOES contain the metadata Payload-Oxum then it should return true!", canQuickVerify);
  }
  
  @Test 
  public void testQuickVerify() throws Exception{
    Path passingRootDir = Paths.get(new File("src/test/resources/bags/v0_94/bag").toURI());
    Bag bag = BagReader.read(passingRootDir);
    
    sut.quicklyVerify(bag, true);
  }
  
  @Test(expected=PayloadOxumDoesNotExistException.class)
  public void testExceptionIsThrownWhenPayloadOxumDoesntExist() throws Exception{
    Bag bag = BagReader.read(rootDir);
    sut.quicklyVerify(bag, true);
    
  }
  
  @Test(expected=InvalidPayloadOxumException.class)
  public void testInvalidByteSizeForQuickVerify() throws Exception{
    Path badRootDir = Paths.get(new File("src/test/resources/badPayloadOxumByteSize/bag").toURI());
    Bag bag = BagReader.read(badRootDir);
    
    sut.quicklyVerify(bag, true);
  }
  
  @Test(expected=InvalidPayloadOxumException.class)
  public void testInvalidFileCountForQuickVerify() throws Exception{
    Path badRootDir = Paths.get(new File("src/test/resources/badPayloadOxumFileCount/bag").toURI());
    Bag bag = BagReader.read(badRootDir);
    
    sut.quicklyVerify(bag, true);
  }
  
  @Test
  public void testStandardSupportedAlgorithms() throws Exception{
    List<String> algorithms = Arrays.asList("md5", "sha1", "sha256", "sha512");
    for(String algorithm : algorithms){
      Manifest manifest = new Manifest(algorithm);
      sut.checkHashes(manifest);
    }
  }
  
  @Test
  public void testBagWithTagFilesInPayloadIsValid() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testVersion0_97IsValid() throws Exception{
    Bag bag = BagReader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testVersion2_0IsValid() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bags/v2_0/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testIsComplete() throws Exception{
    Bag bag = BagReader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenFetchItemsDontExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bad-fetch-bag").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInManifestDontExist").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInManifestException.class)
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInPayloadDirAreNotInManifest").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=CorruptChecksumException.class)
  public void testCorruptPayloadFile() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/corruptPayloadFile").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=CorruptChecksumException.class)
  public void testCorruptTagFile() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/corruptTagFile").toURI());
    Bag bag = BagReader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=MissingBagitFileException.class)
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(Paths.get(folder.getRoot().toURI()));
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    bagitFile.delete();
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=MissingPayloadDirectoryException.class)
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(Paths.get(folder.getRoot().toURI()));
    File dataDir = new File(folder.getRoot(), "data");
    deleteDirectory(Paths.get(dataDir.toURI()));
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=MissingPayloadManifestException.class)
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(Paths.get(folder.getRoot().toURI()));
    File manifestFile = new File(folder.getRoot(), "manifest-md5.txt");
    manifestFile.delete();
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=UnsupportedAlgorithmException.class)
  public void testErrorWhenUnspportedAlgorithmException() throws Exception{
    Path sha3BagDir = Paths.get(getClass().getClassLoader().getResource("sha3Bag").toURI());
    Bag bag = BagReader.read(sha3BagDir);
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=UnsupportedAlgorithmException.class)
  public void testUnsupportedAlgorithmExceptionException() throws Exception{
    Manifest manifest = new Manifest("foo");
    
    sut.checkHashes(manifest);
  }
  
  @Test
  public void testAddSHA3SupportViaExtension() throws Exception{
    Path sha3BagDir = Paths.get(new File("src/test/resources/sha3Bag").toURI());
    Bag bag = BagReader.read(sha3BagDir);
    BagVerifier extendedSut = new BagVerifier(Arrays.asList(new SHA3Hasher()));
    extendedSut.isValid(bag, true);
  }
  
  @Test(expected=FileNotInManifestException.class)
  public void testNotALlFilesListedInAllManifestsThrowsException() throws Exception{
    Path bagDir = Paths.get(new File("src/test/resources/notAllFilesListedInAllManifestsBag").toURI());
    Bag bag = BagReader.read(bagDir);
    sut.isValid(bag, true);
  }
  
  /*
   * Technically valid but highly discouraged
   */
  @Test
  public void testManifestsWithLeadingDotSlash() throws Exception{
    Path bagPath = Paths.get(new File("src/test/resources/bag-with-leading-dot-slash-in-manifest").toURI());
    Bag bag = BagReader.read(bagPath);
    
    sut.isValid(bag, true);
  }
  
  private void copyBagToTestFolder() throws Exception{
    Files.walk(rootDir).forEach(path ->{
      try {
          Files.copy(path, Paths.get(path.toString().replace(
              rootDir.toString(),
              folder.getRoot().toString())));
      } catch (Exception e) {}});
  }
  
  private void deleteDirectory(Path directory) throws Exception{
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    });
  }

}
