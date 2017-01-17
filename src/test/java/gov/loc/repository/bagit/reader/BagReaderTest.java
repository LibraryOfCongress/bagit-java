package gov.loc.repository.bagit.reader;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;

public class BagReaderTest extends Assert{
  @Test
  public void testReadBagWithinABag() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-in-a-bag").toURI());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
  }
  
  @Test
  public void testReadBagWithEncodedNames() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-with-encoded-names").toURI());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(Path file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", Files.exists(file));
      }
    }
  }
  
  @Test
  public void testReadBagWithEscapableCharacter() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-with-escapable-characters").toURI());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(Path file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", Files.exists(file));
      }
    }
  }
  
  @Test
  public void testReadBagWithDotSlash() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-with-leading-dot-slash-in-manifest").toURI());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(Path file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", Files.exists(file));
      }
    }
  }
  
  @Test
  public void testReadBagWithSpaceAsManifestDelimiter() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-with-space").toURI());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(Path file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", Files.exists(file));
      }
    }
  }
  
  @Test
  public void testReadVersion0_93() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_93/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    assertEquals(new Version(0, 93), bag.getVersion());
    for(SimpleImmutableEntry<String, String> keyValue : bag.getMetadata()){
      if("Payload-Oxum".equals(keyValue.getKey())){
        assertEquals("25.5", keyValue.getValue());
      }
    }
  }
  
  @Test
  public void testReadVersion0_94() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_94/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    assertEquals(new Version(0, 94), bag.getVersion());
    for(SimpleImmutableEntry<String, String> keyValue : bag.getMetadata()){
      if("Payload-Oxum".equals(keyValue.getKey())){
        assertEquals("25.5", keyValue.getValue());
      }
    }
  }
  
  @Test
  public void testReadVersion0_95() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_95/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    assertEquals(new Version(0, 95), bag.getVersion());
    for(SimpleImmutableEntry<String, String> keyValue : bag.getMetadata()){
      if("Package-Size".equals(keyValue.getKey())){
        assertEquals("260 GB", keyValue.getValue());
      }
    }
  }

  @Test
  public void testReadISO_8859_1Encoding() throws Exception{
    List<SimpleImmutableEntry<String, String>> expectedMetaData = new ArrayList<>();
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Bag-Software-Agent","bagit.py <http://github.com/libraryofcongress/bagit-python>"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Bagging-Date","2016-02-26"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Contact-Email","cadams@loc.gov"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Contact-Name","Chris Adams"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Payload-Oxum","58.2"));
    
    Path bagPath = Paths.get(new File("src/test/resources/ISO-8859-1-encodedBag").toURI());
    Bag bag = BagReader.read(bagPath);
    assertNotNull(bag);
    assertEquals(StandardCharsets.ISO_8859_1, bag.getFileEncoding());
    assertEquals(expectedMetaData, bag.getMetadata());
  }
  
  @Test
  public void testReadUTF_16_Encoding() throws Exception{
    List<SimpleImmutableEntry<String, String>> expectedMetaData = new ArrayList<>();
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Bag-Software-Agent","bagit.py <http://github.com/libraryofcongress/bagit-python>"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Bagging-Date","2016-02-26"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Contact-Email","cadams@loc.gov"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Contact-Name","Chris Adams"));
    expectedMetaData.add(new SimpleImmutableEntry<String, String>("Payload-Oxum","58.2"));
    
    Path bagPath = Paths.get(new File("src/test/resources/UTF-16-encoded-tag-files").toURI());
    
    List<FetchItem> expectedFetchItems = new ArrayList<>();
    expectedFetchItems.add(new FetchItem(new URL("http://localhost/foo/data/dir1/test3.txt"), -1l, bagPath.resolve("data/dir1/test3.txt")));
    
    Bag bag = BagReader.read(bagPath);
    assertNotNull(bag);
    assertEquals(StandardCharsets.UTF_16, bag.getFileEncoding());
    assertEquals(expectedMetaData, bag.getMetadata());
    assertEquals(expectedFetchItems, bag.getItemsToFetch());
  }
  
  @Test
  public void testReadVersion0_97Bag() throws Exception{
    Path rootBag = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
    Path[] payloadFiles = new Path[]{rootBag.resolve("data/dir1/test3.txt"), rootBag.resolve("data/dir2/dir3/test5.txt"), 
        rootBag.resolve("data/dir2/test4.txt"), rootBag.resolve("data/test1.txt"), rootBag.resolve("data/test2.txt")};
    
    Bag returnedBag = BagReader.read(rootBag);
    
    assertNotNull(returnedBag);
    assertEquals(new Version(0, 97), returnedBag.getVersion());
    Manifest payloadManifest = (Manifest) returnedBag.getPayLoadManifests().toArray()[0];
    for(Path payloadFile : payloadFiles){
      assertTrue(payloadManifest.getFileToChecksumMap().containsKey(payloadFile));
    }
  }
  
  @Test
  public void testReadVersion2_0Bag() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v2_0/bag").toURI());
    Path[] payloadFiles = new Path[]{rootBag.resolve("dir1/test3.txt"), rootBag.resolve("dir2/dir3/test5.txt"), 
        rootBag.resolve("dir2/test4.txt"), rootBag.resolve("test1.txt"), rootBag.resolve("test2.txt")};
    
    Bag returnedBag = BagReader.read(rootBag);
    
    assertNotNull(returnedBag);
    assertEquals(new Version(2, 0), returnedBag.getVersion());
    Manifest payloadManifest = (Manifest) returnedBag.getPayLoadManifests().toArray()[0];
    for(Path payloadFile : payloadFiles){
      assertTrue("payload manifest should contain " + payloadFile, payloadManifest.getFileToChecksumMap().containsKey(payloadFile));
    }
  }
}
