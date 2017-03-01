package gov.loc.repository.bagit.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class BagTest extends Assert {

  @Test
  public void testToString() throws MalformedURLException{
    String expectedToString = "Bag [version=-1.-1, fileEncoding=UTF-8, payLoadManifests=[Manifest [algorithm=MD5, fileToChecksumMap={}] ], tagManifests=[Manifest [algorithm=MD5, fileToChecksumMap={}] ], itemsToFetch=[http://www.wiki.com - foo], metadata=[]]";
    Bag bag = new Bag();
    bag.getPayLoadManifests().add(new Manifest(StandardSupportedAlgorithms.MD5));
    bag.getTagManifests().add(new Manifest(StandardSupportedAlgorithms.MD5));
    bag.getItemsToFetch().add(new FetchItem(new URL("http://www.wiki.com"), -1l, Paths.get("foo")));
    
    assertEquals(expectedToString, bag.toString());
  }
  
  @Test
  public void testHashCodeAreSameForNewBags(){
    Bag bag1 = new Bag();
    Bag bag2 = new Bag();
    
    assertEquals(bag1.hashCode(), bag2.hashCode());
  }
  
  
  @Test
  public void testEqualsShouldReturnTrueWhenBothAreNew(){
    Bag bag1 = new Bag();
    Bag bag2 = new Bag();
    
    assertTrue(bag1.equals(bag2));
  }
  
  @Test
  public void testEqualsShouldReturnTrueWhenUsingConstructor(){
    Bag bag1 = new Bag();
    bag1.setVersion(new Version(99, 99));
    
    Bag bag2 = new Bag(bag1);
    
    assertTrue(bag1.equals(bag2));
  }
  
  @Test
  public void testEqualsShouldReturnTrueWhenSameObject(){
    Bag bag1 = new Bag();
    
    assertTrue(bag1.equals(bag1));
  }
  
  @Test
  public void testEqualsShouldReturnFalseWhenNull(){
    Bag bag1 = new Bag();
    
    assertFalse(bag1.equals(null));
  }
  
  @Test
  public void testEqualsShouldReturnFalseWhenNotABag(){
    Bag bag1 = new Bag();
    
    assertFalse(bag1.equals("foo"));
  }
  
  @Test
  public void foo(){
    List<SimpleImmutableEntry<String, String>> list = new ArrayList<>();
    list.add(new SimpleImmutableEntry<>("foo", "bar"));
    list.add(new SimpleImmutableEntry<>("bar", "ham"));
    list.add(new SimpleImmutableEntry<>("ham", "hungry"));
    
    SimpleImmutableEntry<String, String> entryToRemove = null;
    for(final SimpleImmutableEntry<String, String> entry : list){
      if(!"ham".equals(entry.getKey())){
        entryToRemove = entry;
        continue;
      }
    }
    if(entryToRemove != null){
      list.remove(entryToRemove);
    }
    
    System.err.println(list);
  }
}
