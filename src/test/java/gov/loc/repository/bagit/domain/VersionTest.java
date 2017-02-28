package gov.loc.repository.bagit.domain;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest extends Assert {
  
  @Test
  public void testToString(){
    Version v = new Version(0, 0);
    String expectedString = "0.0";
    
    assertEquals(expectedString, v.toString());
  }
  
  @Test
  public void testHashCodeReturnsSameValueForEqualObjects(){
    Version v1 = new Version(0, 0);
    Version v2 = new Version(0, 0);
    
    assertEquals(v1.hashCode(), v2.hashCode());
  }
  
  @Test
  public void testHashCodeReturnsDifferentValueForDifferentObjects(){
    Version v1 = new Version(0, 0);
    Version v2 = new Version(0, 1);
    
    assertNotEquals(v1.hashCode(), v2.hashCode());
  }
  
  @Test
  public void testCompareToWithOnlyMinorVersions(){
    Version smallest = new Version(0, 0);
    Version middle = new Version(0, 1);
    Version largest = new Version(0, 2);
    
    assertEquals(0, smallest.compareTo(smallest));
    assertEquals(-1, smallest.compareTo(middle));
    assertEquals(-1, smallest.compareTo(largest));
    
    assertEquals(1, middle.compareTo(smallest));
    assertEquals(0, middle.compareTo(middle));
    assertEquals(-1, middle.compareTo(largest));
    
    assertEquals(1, largest.compareTo(smallest));
    assertEquals(1, largest.compareTo(middle));
    assertEquals(0, largest.compareTo(largest));
  }
  
  @Test
  public void testCompareToWithOnlyMajorVersions(){
    Version smallest = new Version(0, 0);
    Version middle = new Version(1, 0);
    Version largest = new Version(2, 0);
    
    assertEquals(0, smallest.compareTo(smallest));
    assertEquals(-1, smallest.compareTo(middle));
    assertEquals(-1, smallest.compareTo(largest));
    
    assertEquals(1, middle.compareTo(smallest));
    assertEquals(0, middle.compareTo(middle));
    assertEquals(-1, middle.compareTo(largest));
    
    assertEquals(1, largest.compareTo(smallest));
    assertEquals(1, largest.compareTo(middle));
    assertEquals(0, largest.compareTo(largest));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    Version v = new Version(0, 0);
    assertTrue(v.equals(v));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    Version v = new Version(0, 0);
    assertFalse(v.equals(null));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNotAVersion(){
    Version v = new Version(0, 0);
    assertFalse(v.equals("foo"));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    Version v1 = new Version(0, 0);
    Version v2 = new Version(0, 0);
    
    assertTrue(v1.equals(v2));
  }
  
  @Test
  public void testIsNewer(){
    Version older = new Version(0, 0);
    Version newer = new Version(0, 1);
    
    assertTrue(newer.isNewer(older));
    
    older = new Version(1, 0);
    newer = new Version(1, 1);
    assertTrue(newer.isNewer(older));
  }
  
  @Test
  public void testIsSameOrNewer(){
    Version same1 = new Version(0, 0);
    Version same2 = new Version(0, 0);
    assertTrue(same1.isSameOrNewer(same2));
  }
  
  @Test
  public void testIsOlder(){
    Version older = new Version(0, 0);
    Version newer = new Version(0, 1);
    
    assertTrue(older.isOlder(newer));
    
    older = new Version(1, 0);
    newer = new Version(1, 1);
    assertTrue(older.isOlder(newer));
  }
  
  @Test
  public void testIsSameOrOlder(){
    Version same1 = new Version(0, 0);
    Version same2 = new Version(0, 0);
    assertTrue(same1.isSameOrOlder(same2));
  }
}
