package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.domain.Version;

public class BagCreatorTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testBagInPlace() throws IOException, NoSuchAlgorithmException{
    List<File> expectedPayloadFiles = createTestStructure();
    
    Bag bag = BagCreator.bagInPlace(folder.getRoot(), StandardSupportedAlgorithms.MD5, false);
    
    assertEquals(new Version(0, 97), bag.getVersion());
    
    File manifest = new File(folder.getRoot(), "manifest-md5.txt");
    assertTrue(manifest.exists());
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    assertTrue(bagitFile.exists());
    
    for(File expectedPayloadFile : expectedPayloadFiles){
      assertTrue(expectedPayloadFile.exists());
    }
  }
  
  private List<File> createTestStructure() throws IOException{
    File dataDir = new File(folder.getRoot(), "data");
    
    File file1 = folder.newFile("file1.txt");
    file1.createNewFile();
    File dir1 = folder.newFolder("folder1");
    dir1.mkdir();
    File file2 = folder.newFile("file2.txt");
    file2.createNewFile();
    
    File hiddenFile = folder.newFile(".hiddentFile.txt");
    assertTrue(hiddenFile.isHidden());
    
    File hiddenDirectory = folder.newFolder(".hiddenFolder");
    assertTrue(hiddenDirectory + " should be hidden unless on windows", hiddenDirectory.isHidden());
    
    File hiddenFile2 = new File(hiddenDirectory, ".hiddenFile2.txt");
    hiddenFile2.createNewFile();
    File file3 = new File(hiddenDirectory, "file3.txt");
    file3.createNewFile();
    
    return Arrays.asList(new File(dataDir, file1.getName()), new File(dataDir, dir1.getName()), new File(dataDir, file2.getName()));
  }
  
  @Test
  public void testCreateDotBagit() throws IOException, NoSuchAlgorithmException{
    File dotbagitDir = new File(folder.getRoot(), ".bagit");
    File expectedManifestFile = new File(dotbagitDir, "manifest-md5.txt");
    File expectedBagitFile = new File(dotbagitDir, "bagit.txt");
    
    Bag bag = BagCreator.createDotBagit(folder.getRoot(), StandardSupportedAlgorithms.MD5, false);
    assertEquals(new Version(0, 98), bag.getVersion());
    
    assertTrue(expectedBagitFile.exists());
    assertTrue(expectedManifestFile.exists());
  }
}
