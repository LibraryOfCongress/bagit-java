package gov.loc.repository.bagit.domain;

import org.junit.Assert;
import org.junit.Test;

public class ManifestTest extends Assert {

  @Test
  public void testToString(){
    String expected = "Manifest [bagitAlgorithmName=md5, fileToChecksumMap={}]";
    assertEquals(expected, new Manifest("md5").toString());
  }
  
  @Test
  public void testHashCodeReturnsSameValueForEqualObjects(){
    Manifest m1 = new Manifest("md5");
    Manifest m2 = new Manifest("md5");
    
    assertEquals(m1.hashCode(), m2.hashCode());
  }
  
  @Test
  public void testHashCodeReturnsDifferentValueForDifferentObjects(){
    Manifest m1 = new Manifest("md5");
    Manifest m2 = new Manifest("sha1");
    
    assertNotEquals(m1.hashCode(), m2.hashCode());
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    Manifest m1 = new Manifest("md5");
    
    assertTrue(m1.equals(m1));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    Manifest m1 = new Manifest("md5");
    
    assertFalse(m1.equals(null));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentType(){
    Manifest m1 = new Manifest("md5");
    
    assertFalse(m1.equals("Foo"));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    Manifest m1 = new Manifest("md5");
    Manifest m2 = new Manifest("md5");
    
    assertTrue(m1.equals(m2));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentValues(){
    Manifest m1 = new Manifest("md5");
    Manifest m2 = new Manifest("sha1");
    
    assertFalse(m1.equals(m2));
  }
}
