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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
  
  @Test
  public void testToString(){
    Version v = new Version(0, 0);
    String expectedString = "0.0";
    
    Assertions.assertEquals(expectedString, v.toString());
  }
  
  @Test
  public void testHashCodeReturnsSameValueForEqualObjects(){
    Version v1 = new Version(0, 0);
    Version v2 = new Version(0, 0);
    
    Assertions.assertEquals(v1.hashCode(), v2.hashCode());
  }
  
  @Test
  public void testHashCodeReturnsDifferentValueForDifferentObjects(){
    Version v1 = new Version(0, 0);
    Version v2 = new Version(0, 1);
    
    Assertions.assertNotEquals(v1.hashCode(), v2.hashCode());
  }
  
  @Test
  public void testCompareToWithOnlyMinorVersions(){
    Version smallest = new Version(0, 0);
    Version middle = new Version(0, 1);
    Version largest = new Version(0, 2);
    
    Assertions.assertEquals(0, smallest.compareTo(smallest));
    Assertions.assertEquals(-1, smallest.compareTo(middle));
    Assertions.assertEquals(-1, smallest.compareTo(largest));
    
    Assertions.assertEquals(1, middle.compareTo(smallest));
    Assertions.assertEquals(0, middle.compareTo(middle));
    Assertions.assertEquals(-1, middle.compareTo(largest));
    
    Assertions.assertEquals(1, largest.compareTo(smallest));
    Assertions.assertEquals(1, largest.compareTo(middle));
    Assertions.assertEquals(0, largest.compareTo(largest));
  }
  
  @Test
  public void testCompareToWithOnlyMajorVersions(){
    Version smallest = new Version(0, 0);
    Version middle = new Version(1, 0);
    Version largest = new Version(2, 0);
    
    Assertions.assertEquals(0, smallest.compareTo(smallest));
    Assertions.assertEquals(-1, smallest.compareTo(middle));
    Assertions.assertEquals(-1, smallest.compareTo(largest));
    
    Assertions.assertEquals(1, middle.compareTo(smallest));
    Assertions.assertEquals(0, middle.compareTo(middle));
    Assertions.assertEquals(-1, middle.compareTo(largest));
    
    Assertions.assertEquals(1, largest.compareTo(smallest));
    Assertions.assertEquals(1, largest.compareTo(middle));
    Assertions.assertEquals(0, largest.compareTo(largest));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    Version v = new Version(0, 0);
    Assertions.assertTrue(v.equals(v));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    Version v = new Version(0, 0);
    Assertions.assertFalse(v.equals(null));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNotAVersion(){
    Version v = new Version(0, 0);
    Assertions.assertFalse(v.equals("foo"));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    Version v1 = new Version(0, 0);
    Version v2 = new Version(0, 0);
    
    Assertions.assertTrue(v1.equals(v2));
  }
  
  @Test
  public void testIsNewer(){
    Version older = new Version(0, 0);
    Version newer = new Version(0, 1);
    
    Assertions.assertTrue(newer.isNewer(older));
    
    older = new Version(1, 0);
    newer = new Version(1, 1);
    Assertions.assertTrue(newer.isNewer(older));
  }
  
  @Test
  public void testIsSameOrNewer(){
    Version same1 = new Version(0, 0);
    Version same2 = new Version(0, 0);
    Assertions.assertTrue(same1.isSameOrNewer(same2));
  }
  
  @Test
  public void testIsOlder(){
    Version older = new Version(0, 0);
    Version newer = new Version(0, 1);
    
    Assertions.assertTrue(older.isOlder(newer));
    
    older = new Version(1, 0);
    newer = new Version(1, 1);
    Assertions.assertTrue(older.isOlder(newer));
  }
  
  @Test
  public void testIsSameOrOlder(){
    Version same1 = new Version(0, 0);
    Version same2 = new Version(0, 0);
    Assertions.assertTrue(same1.isSameOrOlder(same2));
  }
}
