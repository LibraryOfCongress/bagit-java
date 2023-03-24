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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;

public class BagTest {

  @Test
  public void testToString() throws MalformedURLException{
    String expectedToString = "Bag [version=-1.-1, fileEncoding=UTF-8, payLoadManifests=[Manifest [algorithm=MD5, fileToChecksumMap={}] ], tagManifests=[Manifest [algorithm=MD5, fileToChecksumMap={}] ], itemsToFetch=[http://www.wiki.com - foo], metadata=[]]";
    Bag bag = new Bag();
    bag.getPayLoadManifests().add(new Manifest(StandardSupportedAlgorithms.MD5));
    bag.getTagManifests().add(new Manifest(StandardSupportedAlgorithms.MD5));
    bag.getItemsToFetch().add(new FetchItem(new URL("http://www.wiki.com"), -1l, Paths.get("foo")));
    
    Assertions.assertEquals(expectedToString, bag.toString());
  }
  
  @Test
  public void testHashCodeAreSameForNewBags(){
    Bag bag1 = new Bag();
    Bag bag2 = new Bag();
    
    Assertions.assertEquals(bag1.hashCode(), bag2.hashCode());
  }
  
  
  @Test
  public void testEqualsShouldReturnTrueWhenBothAreNew(){
    Bag bag1 = new Bag();
    Bag bag2 = new Bag();
    
    Assertions.assertTrue(bag1.equals(bag2));
  }
  
  @Test
  public void testEqualsShouldReturnTrueWhenUsingConstructor(){
    Bag bag1 = new Bag();
    bag1.setVersion(new Version(99, 99));
    
    Bag bag2 = new Bag(bag1);
    
    Assertions.assertTrue(bag1.equals(bag2));
  }
  
  @Test
  public void testEqualsShouldReturnTrueWhenSameObject(){
    Bag bag1 = new Bag();
    
    Assertions.assertTrue(bag1.equals(bag1));
  }
  
  @Test
  public void testEqualsShouldReturnFalseWhenNull(){
    Bag bag1 = new Bag();
    
    Assertions.assertFalse(bag1.equals(null));
  }
  
  @Test
  public void testEqualsShouldReturnFalseWhenNotABag(){
    Bag bag1 = new Bag();
    
    Assertions.assertFalse(bag1.equals("foo"));
  }
}
