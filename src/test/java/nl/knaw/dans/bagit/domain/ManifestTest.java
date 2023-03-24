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

import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;

public class ManifestTest {

  @Test
  public void testToString(){
    String expected = "Manifest [algorithm=MD5, fileToChecksumMap={}]";
    Assertions.assertEquals(expected, new Manifest(StandardSupportedAlgorithms.MD5).toString());
  }
  
  @Test
  public void testHashCodeReturnsSameValueForEqualObjects(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    Manifest m2 = new Manifest(StandardSupportedAlgorithms.MD5);
    
    Assertions.assertEquals(m1.hashCode(), m2.hashCode());
  }
  
  @Test
  public void testHashCodeReturnsDifferentValueForDifferentObjects(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    Manifest m2 = new Manifest(StandardSupportedAlgorithms.SHA1);
    
    Assertions.assertNotEquals(m1.hashCode(), m2.hashCode());
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    
    Assertions.assertTrue(m1.equals(m1));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    
    Assertions.assertFalse(m1.equals(null));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentType(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    
    Assertions.assertFalse(m1.equals("Foo"));
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    Manifest m2 = new Manifest(StandardSupportedAlgorithms.MD5);
    
    Assertions.assertTrue(m1.equals(m2));
  }
  
  @Test
  public void testEqualsReturnsFalseWhenDifferentValues(){
    Manifest m1 = new Manifest(StandardSupportedAlgorithms.MD5);
    Manifest m2 = new Manifest(StandardSupportedAlgorithms.SHA1);
    
    Assertions.assertFalse(m1.equals(m2));
  }
}
