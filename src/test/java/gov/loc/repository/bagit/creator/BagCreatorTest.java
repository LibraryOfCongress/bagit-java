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

import gov.loc.repository.bagit.domain.StandardSupportedAlgorithms;

public class BagCreatorTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testBagInPlace() throws IOException, NoSuchAlgorithmException{
    List<File> expectedPayloadFiles = createTestStructure();
    
    BagCreator.bagInPlace(folder.getRoot(), StandardSupportedAlgorithms.MD5, false);
    
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
    
    return Arrays.asList(new File(dataDir, file1.getName()), new File(dataDir, dir1.getName()), new File(dataDir, file2.getName()));
  }
}
