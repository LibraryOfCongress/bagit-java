/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.domain;

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
