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
  public void testWriteVersion97() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = reader.read(rootDir); 
    File bagitDir = folder.newFolder();
    
    BagWriter.write(bag, Paths.get(bagitDir.toURI()));
    assertTrue(bagitDir.exists());
  }
  
  @Test
  public void testWriteVersion98() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_98/bag").toURI());
    Bag bag = reader.read(rootDir);
    File dotbagitDir = new File(folder.getRoot(), ".bagit");
    
    BagWriter.write(bag, Paths.get(folder.getRoot().toURI()));
    assertTrue(dotbagitDir.exists());
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
    List<Pair<String, String>> metadata = new ArrayList<>();
    metadata.add(new Pair<>("key1", "value1"));
    metadata.add(new Pair<>("key2", "value2"));
    metadata.add(new Pair<>("key3", "value3"));
    
    assertFalse(bagInfo.exists());
    BagWriter.writeBagitInfoFile(metadata, Paths.get(rootDir.toURI()), StandardCharsets.UTF_8.name());
    assertTrue(bagInfo.exists());
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
  public void testGetPathRelativeToDataDir(){
    String expectedPath = "data/one/two/buckleMyShoe.txt";
    Path file = Paths.get("/foo/bar/ham/", expectedPath);
    
    String actualPath = BagWriter.getPathRelativeToDataDir(file);
    assertEquals(expectedPath + " should be equal to " + actualPath, expectedPath, actualPath);
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
    BagWriter.writeTagManifests(tagManifests, Paths.get(outputDir.toURI()), StandardCharsets.UTF_8.name());
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
    Path output = Paths.get(folder.newFolder().toURI());
    Path dataDir = output.resolve("data");
    
    BagWriter.write(bag, output);
    assertTrue(Files.exists(dataDir));
  }
}
