package gov.loc.repository.bagit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;

public class FooTest extends Assert{
  
  @Test
  public void testWhichIsFaster(){
    File root = new File("/Users/jscancella/development/repos/bagit-java/src/test/resources/bags/v0_96");
    
    LocalDateTime start = LocalDateTime.now();
    root.listFiles();
    long delta = ChronoUnit.NANOS.between(start, LocalDateTime.now());
    System.err.println("listFiles() took " + delta + " ns");
    
    start = LocalDateTime.now();
    root.list();
    delta = ChronoUnit.NANOS.between(start, LocalDateTime.now());
    System.err.println("list() took      " + delta + " ns");
  }
  
  @Test
  public void testCreatingUrlFromFetch() throws MalformedURLException{
    URL url = new URL("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt");
    System.err.println(url);
  }

  @Test
  public void testCreateNewBagFromOld() throws Exception{
    Bag bag = new Bag();
    bag.setItemsToFetch(createItemsToFetch());
    bag.setMetadata(createMetadata());
    bag.setPayLoadManifests(createPayloadManifests());
    bag.setTagManifests(createTagManifests());
    
    Bag newBag = new Bag(bag);
    assertEquals(bag, newBag);
  }
  
  private List<FetchItem> createItemsToFetch() throws Exception{
    URL url = new File("/tmp/foo").toURI().toURL();
    return Arrays.asList(new FetchItem(url, 0l, "foo"));
  }
  
  private LinkedHashMap<String, String> createMetadata(){
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("bar", "ham");
    return map;
  }
  
  private Set<Manifest> createPayloadManifests(){
    Set<Manifest> payload = new HashSet<>();
    Manifest manifest = new Manifest("md5");
    HashMap<File, String> fileToChecksumMap = new HashMap<>();
    fileToChecksumMap.put(new File("/tmp/coke"), "cola");
    manifest.setFileToChecksumMap(fileToChecksumMap);
    return payload;
  }
  
  private Set<Manifest> createTagManifests(){
    Set<Manifest> payload = new HashSet<>();
    Manifest manifest = new Manifest("md5");
    HashMap<File, String> fileToChecksumMap = new HashMap<>();
    fileToChecksumMap.put(new File("/tmp/pepsi"), "cola");
    manifest.setFileToChecksumMap(fileToChecksumMap);
    return payload;
  }
}
