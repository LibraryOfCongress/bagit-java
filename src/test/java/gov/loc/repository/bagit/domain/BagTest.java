package gov.loc.repository.bagit.domain;

import org.junit.Assert;
import org.junit.Test;

public class BagTest extends Assert {

  @Test
  public void testToString(){
    String expectedToString = "Bag [version=-1.-1, fileEncoding=UTF-8, payLoadManifests=[], tagManifests=[], itemsToFetch=[], metadata=[]]";
    
    assertEquals(expectedToString, new Bag().toString());
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
}
