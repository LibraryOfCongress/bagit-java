package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class BagCreatorTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testBagInPlace() throws IOException, NoSuchAlgorithmException{
    List<Path> expectedPayloadFiles = createTestStructure();
    
    Bag bag = BagCreator.bagInPlace(Paths.get(folder.getRoot().toURI()), StandardSupportedAlgorithms.MD5, false);
    
    assertEquals(new Version(0, 97), bag.getVersion());
    
    File expectedManifest = new File(folder.getRoot(), "manifest-md5.txt");
    assertTrue(expectedManifest.exists());
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    assertTrue(bagitFile.exists());
    
    for(Manifest manifest : bag.getPayLoadManifests()){
      for(Path expectedPayloadFile : manifest.getFileToChecksumMap().keySet()){
        assertTrue(expectedPayloadFiles.contains(expectedPayloadFile));
      }
    }
  }
  
  private List<Path> createTestStructure() throws IOException{
    Path rootDir = Paths.get(folder.getRoot().toURI());
    Path dataDir = rootDir.resolve("data");
    
    File file1 = folder.newFile("file1.txt");
    Path file1Path = Paths.get(file1.toURI());
    file1.createNewFile();
    File dir1 = folder.newFolder("folder1");
    dir1.mkdir();
    File file2 = folder.newFile("file2.txt");
    Path file2Path = Paths.get(file2.toURI());
    file2.createNewFile();
    
    File hiddenFile = folder.newFile(".hiddentFile.txt");
    assertTrue(hiddenFile.isHidden());
    
    File hiddenDirectory = folder.newFolder(".hiddenFolder");
    assertTrue(hiddenDirectory + " should be hidden unless on windows", hiddenDirectory.isHidden());
    
    File hiddenFile2 = new File(hiddenDirectory, ".hiddenFile2.txt");
    hiddenFile2.createNewFile();
    File file3 = new File(hiddenDirectory, "file3.txt");
    file3.createNewFile();
    
    return Arrays.asList(dataDir.resolve(file1Path.getFileName()), dataDir.resolve(file2Path.getFileName()));
  }
  
  @Test
  public void testCreateDotBagit() throws IOException, NoSuchAlgorithmException{
    Path rootFolderPath = Paths.get(folder.getRoot().toURI());
    Path dotbagitDir = rootFolderPath.resolve(".bagit");
    Path expectedManifestFile = dotbagitDir.resolve("manifest-md5.txt");
    Path expectedBagitFile = dotbagitDir.resolve("bagit.txt");
    
    Bag bag = BagCreator.createDotBagit(rootFolderPath, StandardSupportedAlgorithms.MD5, false);
    assertEquals(new Version(0, 98), bag.getVersion());
    
    assertTrue(Files.exists(expectedBagitFile));
    assertTrue(Files.exists(expectedManifestFile));
  }
}
