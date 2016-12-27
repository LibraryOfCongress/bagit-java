package gov.loc.repository.bagit.writer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.reader.BagReader;

public class BagWriterTest extends PrivateConstructorTest {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private BagReader reader = new BagReader();
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagWriter.class);
  }
  
  @Test
  public void testGetCorrectRelativeOuputPath() throws Exception{
    Path root = Paths.get(folder.newFolder().toURI());
    Bag bag = BagCreator.bagInPlace(root, Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    
    Path testFile = root.resolve("data").resolve("fooFile.txt");
    Files.createFile(testFile);
    Manifest manifest = (Manifest) bag.getPayLoadManifests().toArray()[0];
    manifest.getFileToChecksumMap().put(testFile, "CHECKSUM");
    bag.getPayLoadManifests().add(manifest);
    
    Path newRoot = Paths.get(folder.newFolder().toURI());
    BagWriter.write(bag, newRoot);
    assertTrue(Files.exists(newRoot.resolve("data").resolve("fooFile.txt")));
  }
  
  @Test
  public void testWriteVersion95() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_95/bag").toURI());
    Bag bag = reader.read(rootDir); 
    File bagitDir = folder.newFolder();
    Path bagitDirPath = Paths.get(bagitDir.toURI());
    List<Path> expectedPaths = Arrays.asList(bagitDirPath.resolve("tagmanifest-md5.txt"),
        bagitDirPath.resolve("manifest-md5.txt"),
        bagitDirPath.resolve("bagit.txt"),
        bagitDirPath.resolve("package-info.txt"),
        bagitDirPath.resolve("data"),
        bagitDirPath.resolve("data").resolve("test1.txt"),
        bagitDirPath.resolve("data").resolve("test2.txt"),
        bagitDirPath.resolve("data").resolve("dir1"),
        bagitDirPath.resolve("data").resolve("dir2"), 
        bagitDirPath.resolve("data").resolve("dir1").resolve("test3.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("test4.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3").resolve("test5.txt"));
    
    BagWriter.write(bag, bagitDirPath);
    for(Path expectedPath : expectedPaths){
      assertTrue("Expected " + expectedPath + " to exist!", Files.exists(expectedPath));
    }
  }
  
  @Test
  public void testWriteVersion97() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = reader.read(rootDir); 
    File bagitDir = folder.newFolder();
    Path bagitDirPath = Paths.get(bagitDir.toURI());
    List<Path> expectedPaths = Arrays.asList(bagitDirPath.resolve("tagmanifest-md5.txt"),
        bagitDirPath.resolve("manifest-md5.txt"),
        bagitDirPath.resolve("bagit.txt"),
        bagitDirPath.resolve("bag-info.txt"),
        bagitDirPath.resolve("data"),
        bagitDirPath.resolve("data").resolve("test1.txt"),
        bagitDirPath.resolve("data").resolve("test2.txt"),
        bagitDirPath.resolve("data").resolve("dir1"),
        bagitDirPath.resolve("data").resolve("dir2"), 
        bagitDirPath.resolve("data").resolve("dir1").resolve("test3.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("test4.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3").resolve("test5.txt"),
        bagitDirPath.resolve("addl_tags"),
        bagitDirPath.resolve("addl_tags").resolve("tag1.txt"));
    
    BagWriter.write(bag, bagitDirPath);
    for(Path expectedPath : expectedPaths){
      assertTrue("Expected " + expectedPath + " to exist!", Files.exists(expectedPath));
    }
  }
  
  @Test
  public void testWriteVersion2_0() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v2_0/bag").toURI());
    Bag bag = reader.read(rootDir);
    
    File bagitDir = folder.newFolder();
    Path bagitDirPath = Paths.get(bagitDir.toURI());
    List<Path> expectedPaths = Arrays.asList(bagitDirPath.resolve(".bagit"),
        bagitDirPath.resolve(".bagit").resolve("manifest-md5.txt"),
        bagitDirPath.resolve(".bagit").resolve("bagit.txt"),
        bagitDirPath.resolve(".bagit").resolve("bag-info.txt"),
        bagitDirPath.resolve(".bagit").resolve("tagmanifest-md5.txt"),
        bagitDirPath.resolve("test1.txt"),
        bagitDirPath.resolve("test2.txt"),
        bagitDirPath.resolve("dir1"),
        bagitDirPath.resolve("dir2"), 
        bagitDirPath.resolve("dir1").resolve("test3.txt"),
        bagitDirPath.resolve("dir2").resolve("test4.txt"),
        bagitDirPath.resolve("dir2").resolve("dir3"),
        bagitDirPath.resolve("dir2").resolve("dir3").resolve("test5.txt"));
    
    BagWriter.write(bag, bagitDirPath);
    for(Path expectedPath : expectedPaths){
      assertTrue("Expected " + expectedPath + " to exist!", Files.exists(expectedPath));
    }
  }
  
  @Test
  public void testWriteHoley() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/holey-bag").toURI());
    Bag bag = reader.read(rootDir); 
    File bagitDir = folder.newFolder();
    
    BagWriter.write(bag, Paths.get(bagitDir.toURI()));
    assertTrue(bagitDir.exists());
    
    File fetchFile = new File(bagitDir, "fetch.txt");
    assertTrue(fetchFile.exists());
  }
  
  @Test
  public void testWriteEmptyBagStillCreatesDataDir() throws Exception{
    Bag bag = new Bag();
    bag.setRootDir(Paths.get(folder.newFolder().toURI()));
    Path dataDir = bag.getRootDir().resolve("data");
    
    BagWriter.write(bag, bag.getRootDir());
    assertTrue(Files.exists(dataDir));
  }
}
