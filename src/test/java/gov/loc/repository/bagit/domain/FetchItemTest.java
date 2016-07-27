package gov.loc.repository.bagit.domain;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FetchItemTest extends Assert {
  
  private URL url;
  
  @Before
  public void setup() throws MalformedURLException{
    url = new URL("https://github.com/LibraryOfCongress/bagit-java");
  }

  @Test
  public void testToString() throws MalformedURLException{
    FetchItem item = new FetchItem(url, 1l, "/foo");
    String expected = "https://github.com/LibraryOfCongress/bagit-java 1 /foo";
    
    assertEquals(expected, item.toString());
  }
  
  @Test
  public void testHashCodeReturnsSameValueForEqualObjects(){
    FetchItem item1 = new FetchItem(url, 1l, "/foo");
    FetchItem item2 = new FetchItem(url, 1l, "/foo");
    
    assertEquals(item1.hashCode(), item2.hashCode());
  }
  
  @Test
  public void testHashCodeReturnsDifferentValueForDifferentObjects(){
    FetchItem item1 = new FetchItem(url, 1l, "/foo");
    FetchItem item2 = new FetchItem(url, 1l, "/bar");
    
    assertNotEquals(item1.hashCode(), item2.hashCode());
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    FetchItem item = new FetchItem(url, 1l, "/foo");
    
    assertTrue(item.equals(item));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    FetchItem item = new FetchItem(url, 1l, "/foo");
    
    assertFalse(item.equals(null));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentTypes(){
    FetchItem item = new FetchItem(url, 1l, "/foo");
    
    assertFalse(item.equals("foo"));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    FetchItem item1 = new FetchItem(url, 1l, "/foo");
    FetchItem item2 = new FetchItem(url, 1l, "/foo");
    
    assertTrue(item1.equals(item2));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentValues(){
    FetchItem item1 = new FetchItem(url, 1l, "/foo");
    FetchItem item2 = new FetchItem(url, 1l, "/bar");
    
    assertFalse(item1.equals(item2));
  }
}
