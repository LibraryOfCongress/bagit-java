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
import gov.loc.repository.bagit.util.PathUtils;

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
  /**
   * Windows-safe method for creating new empty file in temp folder and returning Path
   * @param folder TemporaryFolder where new file is to be created
   * @param fileName name of new file
   * @return Path to new file
   * @throws IOException
   */
  private Path createEmptyFile(TemporaryFolder folder, String fileName) throws IOException{
	  Path filePath = null;
	  File file = folder.newFile(fileName);
	  filePath = Paths.get(file.toURI());
	  file.createNewFile();
	  return filePath;
  }
  
  private void hideIfWindows(File file) throws IOException{
	  if (PathUtils.isWindows()){
		  Files.setAttribute(Paths.get(file.toURI()), "dos:hidden", true);
	  }
	  return;
  }
  
  private List<Path> createTestStructure() throws IOException{
    Path rootDir = Paths.get(folder.getRoot().toURI());
    Path dataDir = rootDir.resolve("data");
    
    Path file1Path = createEmptyFile(folder, "file1.txt");
    File dir1 = folder.newFolder("folder1");
    dir1.mkdir();
    Path file2Path = createEmptyFile(folder, "file2.txt");
    
    File hiddenFile = folder.newFile(".hiddentFile.txt");
    hideIfWindows(hiddenFile);
    assertTrue(hiddenFile.isHidden());
    
    File hiddenDirectory = folder.newFolder(".hiddenFolder");
    hideIfWindows(hiddenDirectory);
    assertTrue(hiddenDirectory + " should be hidden unless on windows", hiddenDirectory.isHidden());
    
    File hiddenFile2 = new File(hiddenDirectory, ".hiddenFile2.txt");
    hiddenFile2.createNewFile();
    hideIfWindows(hiddenFile2);
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
