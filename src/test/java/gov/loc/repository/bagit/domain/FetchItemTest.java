package gov.loc.repository.bagit.domain;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FetchItemTest {
  
  private static URL url;
  
  @BeforeAll
  public static void setup() throws MalformedURLException{
    url = new URL("https://github.com/LibraryOfCongress/bagit-java");
  }

  @Test
  public void testToString(){
    FetchItem item = new FetchItem(url, 1l, Paths.get("/foo"));
    String expected = "https://github.com/LibraryOfCongress/bagit-java 1 " + File.separator + "foo";
    
    Assertions.assertEquals(expected, item.toString(), "expected [" + expected + "] but got [" + item.toString() + "]");
  }
  
  @Test
  public void testHashCodeReturnsSameValueForEqualObjects(){
    FetchItem item1 = new FetchItem(url, 1l, Paths.get("/foo"));
    FetchItem item2 = new FetchItem(url, 1l, Paths.get("/foo"));
    
    Assertions.assertEquals(item1.hashCode(), item2.hashCode());
  }
  
  @Test
  public void testHashCodeReturnsDifferentValueForDifferentObjects(){
    FetchItem item1 = new FetchItem(url, 1l, Paths.get("/foo"));
    FetchItem item2 = new FetchItem(url, 1l, Paths.get("/bar"));
    
    Assertions.assertNotEquals(item1.hashCode(), item2.hashCode());
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    FetchItem item = new FetchItem(url, 1l, Paths.get("/foo"));
    
    Assertions.assertTrue(item.equals(item));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    FetchItem item = new FetchItem(url, 1l, Paths.get("/foo"));
    
    Assertions.assertFalse(item.equals(null));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentTypes(){
    FetchItem item = new FetchItem(url, 1l, Paths.get("/foo"));
    
    Assertions.assertFalse(item.equals("foo"));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    FetchItem item1 = new FetchItem(url, 1l, Paths.get("/foo"));
    FetchItem item2 = new FetchItem(url, 1l, Paths.get("/foo"));
    
    Assertions.assertTrue(item1.equals(item2));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentValues(){
    FetchItem item1 = new FetchItem(url, 1l, Paths.get("/foo"));
    FetchItem item2 = new FetchItem(url, 1l, Paths.get("/bar"));
    
    Assertions.assertFalse(item1.equals(item2));
  }
}
