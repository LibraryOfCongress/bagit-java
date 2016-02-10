package gov.loc.repository.bagit.reader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.SupportedAlgorithms;

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

  @Test
  public void testReadFetchWithNoSizeSpecified() throws IOException{
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
  public void testReadFetchWithSizeSpecified() throws IOException{
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
  public void testReadBagInfo() throws IOException{
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
    
    File bagInfoFile = new File(getClass().getClassLoader().getResource("baginfoFiles/bag-info-0.97.txt").getFile());
    Bag returnedBag = BagReader.readBagInfo(bagInfoFile, new Bag());
    
    assertEquals(expectedValues, returnedBag.getMetadata());
  }
  
  @Test
  public void testReadChecksumFileMap() throws IOException{
    File manifestFile = new File(getClass().getClassLoader().getResource("manifestFiles/manifest-md5-0.97.txt").getFile());
    HashMap<File, String> expectedMap = new HashMap<>();
    expectedMap.put(new File(manifestFile.getParentFile(), "data/dir1/test3.txt"), "8ad8757baa8564dc136c1e07507f4a98");
    expectedMap.put(new File(manifestFile.getParentFile(), "data/dir2/dir3/test5.txt"), "e3d704f3542b44a621ebed70dc0efe13");
    expectedMap.put(new File(manifestFile.getParentFile(), "data/dir2/test4.txt"), "86985e105f79b95d6bc918fb45ec7727");
    expectedMap.put(new File(manifestFile.getParentFile(), "data/test1.txt"), "5a105e8b9d40e1329780d62ea2265d8a");
    expectedMap.put(new File(manifestFile.getParentFile(), "data/test2.txt"), "ad0234829205b9033196ba818f7a872b");
    
    HashMap<File, String> actualMap = BagReader.readChecksumFileMap(manifestFile);
    
    assertEquals(expectedMap, actualMap);
  }
  
  @Test
  public void testReadManifestGetsCorrectAlgorithm() throws IOException{
    File manifestFile = new File(getClass().getClassLoader().getResource("manifestFiles/manifest-md5-0.97.txt").getFile());
    Manifest returnedManifest = BagReader.readManifest(manifestFile);
    
    assertEquals("md5", returnedManifest.getAlgorithm());
  }
  
  @Test
  public void testReadAllManifests() throws IOException{
    File rootBag = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    Bag returnedBag = BagReader.readAllManifests(rootBag, new Bag());
    assertEquals(1, returnedBag.getPayLoadManifests().size());
    assertEquals(1, returnedBag.getTagManifests().size());
  }
  
  @Test
  public void testReadBagitFile()throws IOException{
    File bagitFile = new File(getClass().getClassLoader().getResource("bagitFiles/bagit-0.97.txt").getFile());
    Bag returnedBag = BagReader.readBagitTextFile(bagitFile, new Bag());
    assertEquals("0.97", returnedBag.getVersion());
    assertEquals(StandardCharsets.UTF_8.name(), returnedBag.getFileEncoding());
  }
  
  @Test
  public void testReadVersion0_97Bag() throws IOException{
    File rootBag = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    Bag returnedBag = BagReader.read(rootBag);
    assertNotNull(returnedBag);
  }
  
  @Test
  public void foo() throws Exception{
    BagCreator.bagInPlace(new File("/Users/jscancella/work/baggerTest/foo"), SupportedAlgorithms.SHA256, false);
  }
}
