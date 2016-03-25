package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.reader.BagReader;
import javafx.util.Pair;

public class BagWriterTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private BagReader reader = new BagReader();
  
  @Test
  public void testGetCorrectRelativeOuputPath() throws Exception{
    Path root = Paths.get(folder.newFolder().toURI());
    Bag bag = BagCreator.bagInPlace(root, StandardSupportedAlgorithms.MD5, false);
    
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
  public void testWriteVersion98() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_98/bag").toURI());
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
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag").toURI());
    Bag bag = reader.read(rootDir); 
    File bagitDir = folder.newFolder();
    
    BagWriter.write(bag, Paths.get(bagitDir.toURI()));
    assertTrue(bagitDir.exists());
  }
  
  @Test
  public void testWriteBagitFile() throws Exception{
    File rootDir = folder.newFolder();
    Path rootDirPath = Paths.get(rootDir.toURI());
    Path bagit = rootDirPath.resolve("bagit.txt");
    
    assertFalse(Files.exists(bagit));
    BagWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8.name(), rootDirPath);
    assertTrue(Files.exists(bagit));
    
    //test truncating existing
    long originalModified = Files.getLastModifiedTime(bagit).toMillis();
    long size = Files.size(bagit);
    BagWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8.name(), rootDirPath);
    assertTrue(Files.exists(bagit));
    assertTrue(Files.getLastModifiedTime(bagit) + " should be >= " + originalModified, 
        Files.getLastModifiedTime(bagit).toMillis() >= originalModified);
    assertEquals(size, Files.size(bagit));
  }
  
  @Test
  public void testWriteBagitInfoFile() throws IOException{
    File rootDir = folder.newFolder();
    File bagInfo = new File(rootDir, "bag-info.txt");
    File packageInfo = new File(rootDir, "package-info.txt");
    List<Pair<String, String>> metadata = new ArrayList<>();
    metadata.add(new Pair<>("key1", "value1"));
    metadata.add(new Pair<>("key2", "value2"));
    metadata.add(new Pair<>("key3", "value3"));
    
    assertFalse(bagInfo.exists());
    assertFalse(packageInfo.exists());
    
    BagWriter.writeBagitInfoFile(metadata, new Version(0,96), Paths.get(rootDir.toURI()), StandardCharsets.UTF_8.name());
    assertTrue(bagInfo.exists());
    
    BagWriter.writeBagitInfoFile(metadata, new Version(0,95), Paths.get(rootDir.toURI()), StandardCharsets.UTF_8.name());
    assertTrue(packageInfo.exists());
  }
  
  @Test
  public void testWriteFetchFile() throws Exception{
    File rootDir = folder.newFolder();
    File fetch = new File(rootDir, "fetch.txt");
    URL url = new URL("http://localhost:/foo/bar");
    List<FetchItem> itemsToFetch = Arrays.asList(new FetchItem(url, -1l, "/data/foo/bar"),
        new FetchItem(url, 100l, "/data/foo/bar"));
    
    
    assertFalse(fetch.exists());
    BagWriter.writeFetchFile(itemsToFetch, Paths.get(rootDir.toURI()), StandardCharsets.UTF_8.name());
    assertTrue(fetch.exists());
  }
  
  @Test
  public void testWriteTagManifests() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(Paths.get("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    File outputDir = folder.newFolder();
    File tagManifest = new File(outputDir, "tagmanifest-md5.txt");
    
    assertFalse(tagManifest.exists());
    BagWriter.writeTagManifests(tagManifests, Paths.get(outputDir.toURI()), Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8.name());
    assertTrue(tagManifest.exists());
  }
  
  @Test
  public void testWritePayloadFiles() throws IOException, URISyntaxException{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Path testFile = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").toURI());
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    File outputDir = folder.newFolder();
    File copiedFile = new File(outputDir, "data/dir1/test3.txt");
    
    assertFalse(copiedFile.exists() || copiedFile.getParentFile().exists());
    BagWriter.writePayloadFiles(payloadManifests, Paths.get(outputDir.toURI()), rootDir);
    assertTrue(copiedFile.exists() && copiedFile.getParentFile().exists());
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
