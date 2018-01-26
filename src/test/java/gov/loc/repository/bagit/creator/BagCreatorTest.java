package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.TestUtils;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class BagCreatorTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testBagInPlaceWithFileNamedData() throws IOException, NoSuchAlgorithmException{
    File testFolder = folder.newFolder();
    File dataFile = new File(testFolder, "data");
    Files.createFile(dataFile.toPath());
    
    BagCreator.bagInPlace(Paths.get(testFolder.toURI()), Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    assertTrue(Files.exists(testFolder.toPath().resolve("data").resolve("data")));
  }
  
  @Test
  public void testBagInPlace() throws IOException, NoSuchAlgorithmException{
    TestStructure structure = createTestStructure();
    
    Bag bag = BagCreator.bagInPlace(Paths.get(folder.getRoot().toURI()), Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    
    assertEquals(new Version(0, 97), bag.getVersion());
    
    File expectedManifest = new File(folder.getRoot(), "manifest-md5.txt");
    assertTrue(expectedManifest.exists());
    
    File expectedTagManifest = new File(folder.getRoot(), "tagmanifest-md5.txt");
    assertTrue(expectedTagManifest.exists());
    
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    assertTrue(bagitFile.exists());
    
    //make sure the hidden folder was not included in the data directory
    File hiddenFolder = new File(bag.getRootDir().resolve("data").toFile(), ".hiddenFolder");
    assertFalse(hiddenFolder.exists());
    
    for(Manifest manifest : bag.getPayLoadManifests()){
      for(Path expectedPayloadFile : manifest.getFileToChecksumMap().keySet()){
        assertTrue(structure.regularPayloadFiles.contains(expectedPayloadFile));
      }
    }
  }
  
  @Test
  public void testBagInPlaceIncludingHidden() throws IOException, NoSuchAlgorithmException{
    TestStructure structure = createTestStructure();
    
    Bag bag = BagCreator.bagInPlace(Paths.get(folder.getRoot().toURI()), Arrays.asList(StandardSupportedAlgorithms.MD5), true);
    
    assertEquals(new Version(0, 97), bag.getVersion());
    
    File expectedManifest = new File(folder.getRoot(), "manifest-md5.txt");
    assertTrue(expectedManifest.exists());
    
    File expectedTagManifest = new File(folder.getRoot(), "tagmanifest-md5.txt");
    assertTrue(expectedTagManifest.exists());
    
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    assertTrue(bagitFile.exists());
    
    for(Manifest manifest : bag.getPayLoadManifests()){
      for(Path expectedPayloadFile : manifest.getFileToChecksumMap().keySet()){
        assertTrue(expectedPayloadFile + " doesn't exist but it should!",
            structure.regularPayloadFiles.contains(expectedPayloadFile) || 
            structure.hiddenPayloadFiles.contains(expectedPayloadFile));
      }
    }
  }
  
  private TestStructure createTestStructure() throws IOException{
    TestStructure structure = new TestStructure();
    
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
    File hiddenDirectory = folder.newFolder(".hiddenFolder");
    
    TestUtils.makeFilesHiddenOnWindows(Paths.get(folder.getRoot().toURI()));
    
    assertTrue(hiddenFile.isHidden());
    assertTrue( hiddenDirectory.isHidden());
    
    File hiddenFile2 = new File(hiddenDirectory, ".hiddenFile2.txt");
    hiddenFile2.createNewFile();
    File file3 = new File(hiddenDirectory, "file3.txt");
    file3.createNewFile();
    
    structure.regularPayloadFiles.add(dataDir.resolve(file1Path.getFileName()));
    structure.regularPayloadFiles.add(dataDir.resolve(file2Path.getFileName()));
    
    structure.hiddenPayloadFiles.add(dataDir.resolve(hiddenFile.getName()));
    Path hiddenDirPath = dataDir.resolve(hiddenDirectory.getName());
    Path hiddenFile2Path = hiddenDirPath.resolve(hiddenFile2.getName());
    structure.hiddenPayloadFiles.add(hiddenFile2Path);
    structure.hiddenPayloadFiles.add(hiddenDirPath.resolve(file3.getName()));
    return structure;
  }
  
  @Test
  public void testCreateDotBagit() throws IOException, NoSuchAlgorithmException{
    createTestStructure();
    
    Path rootFolderPath = Paths.get(folder.getRoot().toURI());
    Path dotbagitDir = rootFolderPath.resolve(".bagit");
    Path expectedManifestFile = dotbagitDir.resolve("manifest-md5.txt");
    Path expectedTagManifestFile = dotbagitDir.resolve("tagmanifest-md5.txt");
    Path expectedBagitFile = dotbagitDir.resolve("bagit.txt");
    
    Bag bag = BagCreator.createDotBagit(rootFolderPath, Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    assertEquals(new Version(2, 0), bag.getVersion());
    
    assertTrue(Files.exists(expectedBagitFile));
    assertTrue(Files.size(expectedBagitFile) > 0);
    
    assertTrue(Files.exists(expectedManifestFile));
    assertTrue(Files.size(expectedManifestFile) > 0);
    
    assertTrue(Files.exists(expectedTagManifestFile));
    assertTrue(Files.size(expectedTagManifestFile) > 0);
  }
  
  private class TestStructure{
    List<Path> regularPayloadFiles = new ArrayList<>();
    List<Path> hiddenPayloadFiles = new ArrayList<>();
  }
}
