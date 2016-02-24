package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import gov.loc.repository.bagit.reader.BagReader;

public class BagWriterTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testWriteVersion97() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    Bag bag = BagReader.read(rootDir); 
    File bagitDir = folder.newFolder();
    
    BagWriter.write(bag, bagitDir);
    assertTrue(bagitDir.exists());
  }
  
  @Test
  public void testWriteVersion98() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_98/bag").getFile());
    Bag bag = BagReader.read(rootDir);
    File dotbagitDir = new File(folder.getRoot(), ".bagit");
    
    BagWriter.write(bag, folder.getRoot());
    assertTrue(dotbagitDir.exists());
  }
  
  @Test
  public void testWriteHoley() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag").getFile());
    Bag bag = BagReader.read(rootDir); 
    File bagitDir = folder.newFolder();
    
    BagWriter.write(bag, bagitDir);
    assertTrue(bagitDir.exists());
  }
  
  @Test
  public void testWriteBagitFile() throws Exception{
    File rootDir = folder.newFolder();
    File bagit = new File(rootDir, "bagit.txt");
    
    assertFalse(bagit.exists());
    BagWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8.name(), rootDir);
    assertTrue(bagit.exists());
    
    //test truncating existing
    long originalModified = bagit.lastModified();
    long size = bagit.getTotalSpace();
    BagWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8.name(), rootDir);
    assertTrue(bagit.exists());
    assertTrue(bagit.lastModified() + " should be >= " + originalModified, 
        bagit.lastModified() >= originalModified);
    assertEquals(size, bagit.getTotalSpace());
  }
  
  @Test
  public void testWriteBagitInfoFile() throws IOException{
    File rootDir = folder.newFolder();
    File bagInfo = new File(rootDir, "bag-info.txt");
    LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
    metadata.put("key1", "value1");
    metadata.put("key2", "value2");
    metadata.put("key3", "value3");
    
    assertFalse(bagInfo.exists());
    BagWriter.writeBagitInfoFile(metadata, rootDir, StandardCharsets.UTF_8.name());
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
    BagWriter.writeFetchFile(itemsToFetch, rootDir, StandardCharsets.UTF_8.name());
    assertTrue(fetch.exists());
  }
  
  @Test
  public void testGetPathRelativeToDataDir(){
    String expectedPath = "data/one/two/buckleMyShoe.txt";
    File file = new File("/foo/bar/ham/" + expectedPath);
    
    String actualPath = BagWriter.getPathRelativeToDataDir(file);
    assertEquals(expectedPath + " should be equal to " + actualPath, expectedPath, actualPath);
  }
  
  @Test
  public void testWriteTagManifests() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest("md5");
    manifest.getFileToChecksumMap().put(new File("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    File outputDir = folder.newFolder();
    File tagManifest = new File(outputDir, "tagmanifest-md5.txt");
    
    assertFalse(tagManifest.exists());
    BagWriter.writeTagManifests(tagManifests, outputDir, StandardCharsets.UTF_8.name());
    assertTrue(tagManifest.exists());
  }
  
  @Test
  public void testWritePayloadFiles() throws IOException{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    File testFile = new File(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").getFile());
    Manifest manifest = new Manifest("md5");
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    File outputDir = folder.newFolder();
    File copiedFile = new File(outputDir, "data/dir1/test3.txt");
    
    assertFalse(copiedFile.exists() || copiedFile.getParentFile().exists());
    BagWriter.writePayloadFiles(payloadManifests, outputDir, rootDir);
    assertTrue(copiedFile.exists() && copiedFile.getParentFile().exists());
  }
}
