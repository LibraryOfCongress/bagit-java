package gov.loc.repository.bagit.reader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.MaliciousManifestException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import javafx.util.Pair;

public class BagReaderTest extends Assert{
  private List<URL> urls;
  private static final List<String> paths = Arrays.asList("data/dir1/test3.txt", "data/dir2/dir3/test5.txt", 
      "data/dir2/test4.txt", "data/test 1.txt", "data/test2.txt");
  
  private BagReader sut;
  
  @Before
  public void setup() throws MalformedURLException{
    sut = new BagReader();
    urls = Arrays.asList(new URL("http://localhost/foo/data/dir1/test3.txt"), 
        new URL("http://localhost/foo/data/dir2/dir3/test5.txt"),
        new URL("http://localhost/foo/data/dir2/test4.txt"),
        new URL("http://localhost/foo/data/test%201.txt"),
        new URL("http://localhost/foo/data/test2.txt"));
  }
  
  @Test(expected=UnparsableVersionException.class)
  public void testParseVersionWithBadVersion() throws UnparsableVersionException{
    sut.parseVersion("someVersionThatIsUnparsable");
  }
  
  @Test
  public void testReadBagWithinABag() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-in-a-bag").toURI());
    Bag bag = sut.read(rootDir);
    assertNotNull(bag);
  }
  
  @Test
  public void testReadBagWithEncodedNames() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/bag-with-encoded-names").toURI());
    Bag bag = sut.read(rootDir);
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
    Bag bag = sut.read(rootDir);
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
    Bag bag = sut.read(rootDir);
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
    Bag bag = sut.read(rootDir);
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
    Bag bag = sut.read(rootDir);
    assertEquals(new Version(0, 93), bag.getVersion());
    for(Pair<String, String> keyValue : bag.getMetadata()){
      if("Payload-Oxum".equals(keyValue.getKey())){
        assertEquals("25.5", keyValue.getValue());
      }
    }
  }
  
  @Test
  public void testReadVersion0_94() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_94/bag").toURI());
    Bag bag = sut.read(rootDir);
    assertEquals(new Version(0, 94), bag.getVersion());
    for(Pair<String, String> keyValue : bag.getMetadata()){
      if("Payload-Oxum".equals(keyValue.getKey())){
        assertEquals("25.5", keyValue.getValue());
      }
    }
  }
  
  @Test
  public void testReadVersion0_95() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_95/bag").toURI());
    Bag bag = sut.read(rootDir);
    assertEquals(new Version(0, 95), bag.getVersion());
    for(Pair<String, String> keyValue : bag.getMetadata()){
      if("Package-Size".equals(keyValue.getKey())){
        assertEquals("260 GB", keyValue.getValue());
      }
    }
  }

  @Test
  public void testReadFetchWithNoSizeSpecified() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("fetchFiles/fetchWithNoSizeSpecified.txt").toURI());
    Bag returnedBag = sut.readFetch(fetchFile, new Bag());
    for(FetchItem item : returnedBag.getItemsToFetch()){
      assertNotNull(item.url);
      assertTrue(urls.contains(item.url));
      
      assertEquals(Long.valueOf(-1), item.length);
      
      assertNotNull(item.path);
      assertTrue(paths.contains(item.path));
    }
  }
  
  @Test
  public void testReadFetchWithSizeSpecified() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("fetchFiles/fetchWithSizeSpecified.txt").toURI());
    Bag returnedBag = sut.readFetch(fetchFile, new Bag());
    for(FetchItem item : returnedBag.getItemsToFetch()){
      assertNotNull(item.url);
      assertTrue(urls.contains(item.url));
      
      assertTrue(item.length > 0);
      
      assertNotNull(item.path);
      assertTrue(paths.contains(item.path));
    }
  }
  
  @Test
  public void testReadBagMetadata() throws Exception{
    List<Pair<String, String>> expectedValues = new ArrayList<>();
    expectedValues.add(new Pair<>("Source-Organization", "Spengler University"));
    expectedValues.add(new Pair<>("Organization-Address", "1400 Elm St., Cupertino, California, 95014"));
    expectedValues.add(new Pair<>("Contact-Name", "Edna Janssen"));
    expectedValues.add(new Pair<>("Contact-Phone", "+1 408-555-1212"));
    expectedValues.add(new Pair<>("Contact-Email", "ej@spengler.edu"));
    expectedValues.add(new Pair<>("External-Description", "Uncompressed greyscale TIFF images from the" + System.lineSeparator() + 
        "         Yoshimuri papers collection."));
    expectedValues.add(new Pair<>("Bagging-Date", "2008-01-15"));
    expectedValues.add(new Pair<>("External-Identifier", "spengler_yoshimuri_001"));
    expectedValues.add(new Pair<>("Bag-Size", "260 GB"));
    expectedValues.add(new Pair<>("Bag-Group-Identifier", "spengler_yoshimuri"));
    expectedValues.add(new Pair<>("Bag-Count", "1 of 15"));
    expectedValues.add(new Pair<>("Internal-Sender-Identifier", "/storage/images/yoshimuri"));
    expectedValues.add(new Pair<>("Internal-Sender-Description", "Uncompressed greyscale TIFFs created from" + System.lineSeparator() + 
        "         microfilm."));
    expectedValues.add(new Pair<>("Bag-Count", "1 of 15")); //test duplicate
    
    Path bagInfoFile = Paths.get(getClass().getClassLoader().getResource("baginfoFiles").toURI());
    List<Pair<String, String>> actualMetadata = sut.readBagMetadata(bagInfoFile, StandardCharsets.UTF_8);
    
    assertEquals(expectedValues, actualMetadata);
  }
  
  @Test
  public void testReadAllManifests() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = new Bag();
    bag.setRootDir(rootBag);
    sut.readAllManifests(rootBag, bag);
    assertEquals(1, bag.getPayLoadManifests().size());
    assertEquals(1, bag.getTagManifests().size());
  }
  
  @Test
  public void testReadISO_8859_1Encoding() throws Exception{
    List<Pair<String, String>> expectedMetaData = new ArrayList<>();
    expectedMetaData.add(new Pair<String, String>("Bag-Software-Agent","bagit.py <http://github.com/libraryofcongress/bagit-python>"));
    expectedMetaData.add(new Pair<String, String>("Bagging-Date","2016-02-26"));
    expectedMetaData.add(new Pair<String, String>("Contact-Email","cadams@loc.gov"));
    expectedMetaData.add(new Pair<String, String>("Contact-Name","Chris Adams"));
    expectedMetaData.add(new Pair<String, String>("Payload-Oxum","58.2"));
    
    Path bagPath = Paths.get(new File("src/test/resources/ISO-8859-1-encodedBag").toURI());
    Bag bag = sut.read(bagPath);
    assertNotNull(bag);
    assertEquals(StandardCharsets.ISO_8859_1, bag.getFileEncoding());
    assertEquals(expectedMetaData, bag.getMetadata());
  }
  
  @Test
  public void testReadUTF_16_Encoding() throws Exception{
    List<Pair<String, String>> expectedMetaData = new ArrayList<>();
    expectedMetaData.add(new Pair<String, String>("Bag-Software-Agent","bagit.py <http://github.com/libraryofcongress/bagit-python>"));
    expectedMetaData.add(new Pair<String, String>("Bagging-Date","2016-02-26"));
    expectedMetaData.add(new Pair<String, String>("Contact-Email","cadams@loc.gov"));
    expectedMetaData.add(new Pair<String, String>("Contact-Name","Chris Adams"));
    expectedMetaData.add(new Pair<String, String>("Payload-Oxum","58.2"));
    
    List<FetchItem> expectedFetchItems = new ArrayList<>();
    expectedFetchItems.add(new FetchItem(new URL("http://localhost/foo/data/dir1/test3.txt"), -1l, "data/dir1/test3.txt"));
    
    Path bagPath = Paths.get(new File("src/test/resources/UTF-16-encoded-tag-files").toURI());
    Bag bag = sut.read(bagPath);
    assertNotNull(bag);
    assertEquals(StandardCharsets.UTF_16, bag.getFileEncoding());
    assertEquals(expectedMetaData, bag.getMetadata());
    assertEquals(expectedFetchItems, bag.getItemsToFetch());
  }
  
  @Test
  public void testReadBagitFile()throws Exception{
    Path bagitFile = Paths.get(getClass().getClassLoader().getResource("bagitFiles/bagit-0.97.txt").toURI());
    Pair<Version, Charset> actualBagitInfo = sut.readBagitTextFile(bagitFile);
    assertEquals(new Version(0, 97), actualBagitInfo.getKey());
    assertEquals(StandardCharsets.UTF_8, actualBagitInfo.getValue());
  }
  
  @Test
  public void testReadVersion0_97Bag() throws Exception{
    Path rootBag = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
    Path[] payloadFiles = new Path[]{rootBag.resolve("data/dir1/test3.txt"), rootBag.resolve("data/dir2/dir3/test5.txt"), 
        rootBag.resolve("data/dir2/test4.txt"), rootBag.resolve("data/test1.txt"), rootBag.resolve("data/test2.txt")};
    
    Bag returnedBag = sut.read(rootBag);
    
    assertNotNull(returnedBag);
    assertEquals(new Version(0, 97), returnedBag.getVersion());
    Manifest payloadManifest = (Manifest) returnedBag.getPayLoadManifests().toArray()[0];
    for(Path payloadFile : payloadFiles){
      assertTrue(payloadManifest.getFileToChecksumMap().containsKey(payloadFile));
    }
  }
  
  @Test
  public void testReadVersion0_98Bag() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v0_98/bag").toURI());
    Path[] payloadFiles = new Path[]{rootBag.resolve("dir1/test3.txt"), rootBag.resolve("dir2/dir3/test5.txt"), 
        rootBag.resolve("dir2/test4.txt"), rootBag.resolve("test1.txt"), rootBag.resolve("test2.txt")};
    
    Bag returnedBag = sut.read(rootBag);
    
    assertNotNull(returnedBag);
    assertEquals(new Version(0, 98), returnedBag.getVersion());
    Manifest payloadManifest = (Manifest) returnedBag.getPayLoadManifests().toArray()[0];
    for(Path payloadFile : payloadFiles){
      assertTrue("payload manifest should contain " + payloadFile, payloadManifest.getFileToChecksumMap().containsKey(payloadFile));
    }
  }
  
  @Test(expected=MaliciousManifestException.class)
  public void testReadMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/upAdirectoryReference.txt").toURI());
    sut.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8);
  }
  
  @Test(expected=InvalidBagMetadataException.class)
  public void testReadInproperIndentedBagMetadataFileThrowsException() throws Exception{
    Path baginfo = Paths.get(getClass().getClassLoader().getResource("badBagMetadata/badIndent.txt").toURI());
    sut.readKeyValuesFromFile(baginfo, ":", StandardCharsets.UTF_8);
  }
  
  @Test(expected=InvalidBagMetadataException.class)
  public void testReadInproperBagMetadataKeyValueSeparatorThrowsException() throws Exception{
    Path baginfo = Paths.get(getClass().getClassLoader().getResource("badBagMetadata/badKeyValueSeparator.txt").toURI());
    sut.readKeyValuesFromFile(baginfo, ":", StandardCharsets.UTF_8);
  }
}
