package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
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
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.reader.BagReader;

public class BagVerifierTest extends Assert{
  static {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
  
  private BagVerifier sut = new BagVerifier();
  private BagReader reader = new BagReader();
  
  @Test
  public void testCanQuickVerify() throws Exception{
    Bag bag = reader.read(rootDir);
    boolean canQuickVerify = sut.canQuickVerify(bag);
    assertFalse("Since " + bag.getRootDir() + " DOES NOT contain the metadata Payload-Oxum then it should return false!", canQuickVerify);
    
    Path passingRootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_94/bag").toURI());
    bag = reader.read(passingRootDir);
    canQuickVerify = sut.canQuickVerify(bag);
    assertTrue("Since " + bag.getRootDir() + " DOES contain the metadata Payload-Oxum then it should return true!", canQuickVerify);
  }
  
  @Test 
  public void testQuickVerify() throws Exception{
    Path passingRootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_94/bag").toURI());
    Bag bag = reader.read(passingRootDir);
    
    sut.quicklyVerify(bag, true);
  }
  
  @Test(expected=PayloadOxumDoesNotExistException.class)
  public void testExceptionIsThrownWhenPayloadOxumDoesntExist() throws Exception{
    Bag bag = reader.read(rootDir);
    sut.quicklyVerify(bag, true);
    
  }
  
  @Test(expected=InvalidPayloadOxumException.class)
  public void testInvalidByteSizeForQuickVerify() throws Exception{
    Path badRootDir = Paths.get(getClass().getClassLoader().getResource("badPayloadOxumByteSize/bag").toURI());
    Bag bag = reader.read(badRootDir);
    
    sut.quicklyVerify(bag, true);
  }
  
  @Test(expected=InvalidPayloadOxumException.class)
  public void testInvalidFileCountForQuickVerify() throws Exception{
    Path badRootDir = Paths.get(getClass().getClassLoader().getResource("badPayloadOxumFileCount/bag").toURI());
    Bag bag = reader.read(badRootDir);
    
    sut.quicklyVerify(bag, true);
  }
  
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
  public void testBagWithTagFilesInPayloadIsValid() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testVersion0_97IsValid() throws Exception{
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testVersion0_98IsValid() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_98/bag").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testIsComplete() throws Exception{
    Bag bag = reader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenFetchItemsDontExist() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/holey-bag").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("filesInManifestDontExist").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInManifestException.class)
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("filesInPayloadDirAreNotInManifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isComplete(bag, true);
  }
  
  @Test(expected=CorruptChecksumException.class)
  public void testCorruptPayloadFile() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("corruptPayloadFile").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=CorruptChecksumException.class)
  public void testCorruptTagFile() throws Exception{
    rootDir = Paths.get(getClass().getClassLoader().getResource("corruptTagFile").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=MissingBagitFileException.class)
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(Paths.get(folder.getRoot().toURI()));
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    bagitFile.delete();
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=MissingPayloadDirectoryException.class)
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(Paths.get(folder.getRoot().toURI()));
    File dataDir = new File(folder.getRoot(), "data");
    deleteDirectory(Paths.get(dataDir.toURI()));
    
    sut.isValid(bag, true);
  }
  
  @Test(expected=MissingPayloadManifestException.class)
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(Paths.get(folder.getRoot().toURI()));
    File manifestFile = new File(folder.getRoot(), "manifest-md5.txt");
    manifestFile.delete();
    
    sut.isValid(bag, true);
  }
  
  @Test
  public void testAddSHA3SupportViaExtension() throws Exception{
    Path sha3BagDir = Paths.get(getClass().getClassLoader().getResource("sha3Bag").toURI());
    MySupportedNameToAlgorithmMapping mapping = new MySupportedNameToAlgorithmMapping();
    BagReader extendedReader = new BagReader(mapping);
    Bag bag = extendedReader.read(sha3BagDir);
    BagVerifier extendedSut = new BagVerifier(mapping);
    extendedSut.isValid(bag, true);
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
