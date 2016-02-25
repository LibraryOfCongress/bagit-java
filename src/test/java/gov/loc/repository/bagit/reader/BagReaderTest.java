package gov.loc.repository.bagit.reader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;

public class BagReaderTest extends Assert{
  private List<URL> urls;
  private static final List<String> paths = Arrays.asList("data/dir1/test3.txt", "data/dir2/dir3/test5.txt", 
      "data/dir2/test4.txt", "data/test 1.txt", "data/test2.txt");
  
  @Before
  public void setup() throws MalformedURLException{
    urls = Arrays.asList(new URL("http://localhost/foo/data/dir1/test3.txt"), 
        new URL("http://localhost/foo/data/dir2/dir3/test5.txt"),
        new URL("http://localhost/foo/data/dir2/test4.txt"),
        new URL("http://localhost/foo/data/test%201.txt"),
        new URL("http://localhost/foo/data/test2.txt"));
  }
  
  @Test(expected=UnparsableVersionException.class)
  public void testParseVersionWithBadVersion() throws UnparsableVersionException{
    BagReader.parseVersion("someVersionThatIsUnparsable");
  }
  
  @Test
  public void testReadBagWithinABag() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag-in-a-bag").getFile());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
  }
  
  @Test
  public void testReadBagWithEncodedNames() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag-with-encoded-names").getFile());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(File file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", file.exists());
      }
    }
  }
  
  @Test
  public void testReadBagWithEscapableCharacter() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag-with-escapable-characters").getFile());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(File file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", file.exists());
      }
    }
  }
  
  @Test
  public void testReadBagWithDotSlash() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag-with-leading-dot-slash-in-manifest").getFile());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(File file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", file.exists());
      }
    }
  }
  
  @Test
  public void testReadBagWithSpaceAsManifestDelimiter() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag-with-space").getFile());
    Bag bag = BagReader.read(rootDir);
    assertNotNull(bag);
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      for(File file : payloadManifest.getFileToChecksumMap().keySet()){
        assertTrue(file + " should exist but it doesn't!", file.exists());
      }
    }
  }
  
  @Test
  public void testReadVersion0_93() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_93/bag").getFile());
    Bag bag = BagReader.read(rootDir);
    assertEquals(new Version(0, 93), bag.getVersion());
    assertEquals("25.5", bag.getMetadata().get("Payload-Oxum"));
  }
  
  @Test
  public void testReadVersion0_94() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_94/bag").getFile());
    Bag bag = BagReader.read(rootDir);
    assertEquals(new Version(0, 94), bag.getVersion());
    assertEquals("25.5", bag.getMetadata().get("Payload-Oxum"));
  }
  
  @Test
  public void testReadVersion0_95() throws Exception{
    File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_95/bag").getFile());
    Bag bag = BagReader.read(rootDir);
    assertEquals(new Version(0, 95), bag.getVersion());
    assertEquals("260 GB", bag.getMetadata().get("Package-Size"));
  }

  @Test
  public void testReadFetchWithNoSizeSpecified() throws Exception{
    File fetchFile = new File(getClass().getClassLoader().getResource("fetchFiles/fetchWithNoSizeSpecified.txt").getFile());
    Bag returnedBag = BagReader.readFetch(fetchFile, new Bag());
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
    File fetchFile = new File(getClass().getClassLoader().getResource("fetchFiles/fetchWithSizeSpecified.txt").getFile());
    Bag returnedBag = BagReader.readFetch(fetchFile, new Bag());
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
    LinkedHashMap<String, String> expectedValues = new LinkedHashMap<>();
    expectedValues.put("Source-Organization", "Spengler University");
    expectedValues.put("Organization-Address", "1400 Elm St., Cupertino, California, 95014");
    expectedValues.put("Contact-Name", "Edna Janssen");
    expectedValues.put("Contact-Phone", "+1 408-555-1212");
    expectedValues.put("Contact-Email", "ej@spengler.edu");
    expectedValues.put("External-Description", "Uncompressed greyscale TIFF images from the\n" + 
        "         Yoshimuri papers collection.");
    expectedValues.put("Bagging-Date", "2008-01-15");
    expectedValues.put("External-Identifier", "spengler_yoshimuri_001");
    expectedValues.put("Bag-Size", "260 GB");
    expectedValues.put("Bag-Group-Identifier", "spengler_yoshimuri");
    expectedValues.put("Bag-Count", "1 of 15");
    expectedValues.put("Internal-Sender-Identifier", "/storage/images/yoshimuri");
    expectedValues.put("Internal-Sender-Description", "Uncompressed greyscale TIFFs created from\n" + 
        "         microfilm.");
    
    File bagInfoFile = new File(getClass().getClassLoader().getResource("baginfoFiles").getFile());
    Bag returnedBag = BagReader.readBagMetadata(bagInfoFile, new Bag());
    
    assertEquals(expectedValues, returnedBag.getMetadata());
  }
  
  @Test
  public void testReadAllManifests() throws Exception{
    File rootBag = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    Bag returnedBag = BagReader.readAllManifests(rootBag, new Bag());
    assertEquals(1, returnedBag.getPayLoadManifests().size());
    assertEquals(1, returnedBag.getTagManifests().size());
  }
  
  @Test
  public void testReadBagitFile()throws Exception{
    File bagitFile = new File(getClass().getClassLoader().getResource("bagitFiles/bagit-0.97.txt").getFile());
    Bag returnedBag = BagReader.readBagitTextFile(bagitFile, new Bag(new Version(0, 96)));
    assertEquals(new Version(0, 97), returnedBag.getVersion());
    assertEquals(StandardCharsets.UTF_8.name(), returnedBag.getFileEncoding());
  }
  
  @Test
  public void testReadVersion0_97Bag() throws Exception{
    File rootBag = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    File[] payloadFiles = new File[]{new File(rootBag, "data/dir1/test3.txt"), new File(rootBag, "data/dir2/dir3/test5.txt"), 
        new File(rootBag, "data/dir2/test4.txt"), new File(rootBag, "data/test1.txt"), new File(rootBag, "data/test2.txt")};
    
    Bag returnedBag = BagReader.read(rootBag);
    
    assertNotNull(returnedBag);
    assertEquals(new Version(0, 97), returnedBag.getVersion());
    Manifest payloadManifest = (Manifest) returnedBag.getPayLoadManifests().toArray()[0];
    for(File payloadFile : payloadFiles){
      assertTrue(payloadManifest.getFileToChecksumMap().containsKey(payloadFile));
    }
  }
  
  @Test
  public void testReadVersion0_98Bag() throws Exception{
    File rootBag = new File(getClass().getClassLoader().getResource("bags/v0_98/bag").getFile());
    File[] payloadFiles = new File[]{new File(rootBag, "dir1/test3.txt"), new File(rootBag, "dir2/dir3/test5.txt"), 
        new File(rootBag, "dir2/test4.txt"), new File(rootBag, "test1.txt"), new File(rootBag, "test2.txt")};
    
    Bag returnedBag = BagReader.read(rootBag);
    
    assertNotNull(returnedBag);
    assertEquals(new Version(0, 98), returnedBag.getVersion());
    Manifest payloadManifest = (Manifest) returnedBag.getPayLoadManifests().toArray()[0];
    for(File payloadFile : payloadFiles){
      assertTrue("payload manifest should contain " + payloadFile, payloadManifest.getFileToChecksumMap().containsKey(payloadFile));
    }
  }
}
