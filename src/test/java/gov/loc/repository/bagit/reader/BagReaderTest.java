package gov.loc.repository.bagit.reader;

import java.net.MalformedURLException;
import java.net.URL;
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
    expectedValues.add(new Pair<>("External-Description", "Uncompressed greyscale TIFF images from the\n" + 
        "         Yoshimuri papers collection."));
    expectedValues.add(new Pair<>("Bagging-Date", "2008-01-15"));
    expectedValues.add(new Pair<>("External-Identifier", "spengler_yoshimuri_001"));
    expectedValues.add(new Pair<>("Bag-Size", "260 GB"));
    expectedValues.add(new Pair<>("Bag-Group-Identifier", "spengler_yoshimuri"));
    expectedValues.add(new Pair<>("Bag-Count", "1 of 15"));
    expectedValues.add(new Pair<>("Internal-Sender-Identifier", "/storage/images/yoshimuri"));
    expectedValues.add(new Pair<>("Internal-Sender-Description", "Uncompressed greyscale TIFFs created from\n" + 
        "         microfilm."));
    expectedValues.add(new Pair<>("Bag-Count", "1 of 15")); //test duplicate
    
    Path bagInfoFile = Paths.get(getClass().getClassLoader().getResource("baginfoFiles").toURI());
    Bag returnedBag = sut.readBagMetadata(bagInfoFile, new Bag());
    
    assertEquals(expectedValues, returnedBag.getMetadata());
  }
  
  @Test
  public void testReadAllManifests() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = new Bag();
    bag.setRootDir(rootBag);
    Bag returnedBag = sut.readAllManifests(rootBag, bag);
    assertEquals(1, returnedBag.getPayLoadManifests().size());
    assertEquals(1, returnedBag.getTagManifests().size());
  }
  
  @Test
  public void testReadBagitFile()throws Exception{
    Path bagitFile = Paths.get(getClass().getClassLoader().getResource("bagitFiles/bagit-0.97.txt").toURI());
    Bag returnedBag = sut.readBagitTextFile(bagitFile, new Bag(new Version(0, 96)));
    assertEquals(new Version(0, 97), returnedBag.getVersion());
    assertEquals(StandardCharsets.UTF_8.name(), returnedBag.getFileEncoding());
  }
  
  @Test
  public void testReadVersion0_97Bag() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
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
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/manifest-md5.txt").toURI());
    sut.readChecksumFileMap(manifestFile, Paths.get("/foo"));
  }
  
  @Test(expected=InvalidBagMetadataException.class)
  public void testReadInproperIndentedBagMetadataFileThrowsException() throws Exception{
    Path baginfo = Paths.get(getClass().getClassLoader().getResource("badBagMetadata/badIndent.txt").toURI());
    sut.readKeyValuesFromFile(baginfo, ":");
  }
  
  @Test(expected=InvalidBagMetadataException.class)
  public void testReadInproperBagMetadataKeyValueSeparatorThrowsException() throws Exception{
    Path baginfo = Paths.get(getClass().getClassLoader().getResource("badBagMetadata/badKeyValueSeparator.txt").toURI());
    sut.readKeyValuesFromFile(baginfo, ":");
  }
}
