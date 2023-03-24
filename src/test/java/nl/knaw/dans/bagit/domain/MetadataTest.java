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

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetadataTest {
  private Metadata sut;
  
  @BeforeEach
  public void setup(){
    sut = new Metadata();
  }

  @Test
  public void testPayloadOxumUpsert(){
    Assertions.assertNull(sut.get("payload-oxum"));
    
    //test inserting
    String payloadOxum = "6.1";
    sut.upsertPayloadOxum(payloadOxum);
    Assertions. assertEquals(Arrays.asList(payloadOxum), sut.get("payload-oxum"));
    
    payloadOxum = "25.2";
    sut.upsertPayloadOxum(payloadOxum);
    Assertions.assertEquals(Arrays.asList(payloadOxum), sut.get("payload-oxum"));
  }
  
  @Test
  public void testCaseInsensitiveAccess(){
    sut.add("key", "value");
    
    Assertions.assertEquals(Arrays.asList("value"), sut.get("KEY"));
    Assertions.assertEquals(Arrays.asList("value"), sut.get("key"));
    Assertions.assertEquals(Arrays.asList("value"), sut.get("Key"));
    Assertions.assertEquals(Arrays.asList("value"), sut.get("kEY"));
  }
  
  @Test
  public void testCaseIsPreserved(){
    String key = "FoOVaLuE";
    String value = "BaRVaLuE";
    sut.add(key, value);
    
    Assertions.assertEquals(key, sut.getAll().get(0).getKey());
    Assertions.assertEquals(value, sut.getAll().get(0).getValue());
  }
  
  @Test
  public void testEquals(){
    sut.add("key", "value");
    
    Metadata differentValues = new Metadata();
    differentValues.add("foo", "bar");
    
    Metadata repeatedValues = new Metadata();
    repeatedValues.add("key", "value");
    repeatedValues.add("key", "value");
    
    Metadata same = new Metadata();
    same.add("key", "value");
    
    Assertions. assertTrue(sut.equals(sut), "should be same since same memory reference");
    Assertions.assertFalse(sut.equals(null), "should not be null");
    Assertions.assertFalse(sut.equals("a string"), "should not equal each other since they are different types");
    Assertions.assertFalse(sut.equals(differentValues), "should not equal each other since they have different values");
    Assertions.assertFalse(sut.equals(repeatedValues), "should not equal each other since one has duplicate values");
    Assertions.assertTrue(sut.equals(same), "should be equal since they hold the same values");
  }
  
  @Test
  public void testRemove(){
    sut.add("key", "value");
    sut.add("key", "value");
    sut.add("key", "value");
    
    Assertions.assertEquals(3, sut.getList().size());
    Assertions.assertEquals(3, sut.getMap().get("KEY").size());
    Assertions.assertEquals(3, sut.getAll().size());
    
    sut.remove("key");
    Assertions.assertEquals(0, sut.getList().size());
    Assertions.assertNull(sut.getMap().get("KEY"));
    Assertions.assertEquals(0, sut.getAll().size());
  }
  
  @Test
  public void testAddingMultiplePayloadOxumJustUpsertsInstead(){
    sut.add("Payload-Oxum", "16.1");
    sut.add("Payload-Oxum", "20.5");
    sut.add("Payload-Oxum", "100.7");
    
    Assertions.assertEquals(Arrays.asList("100.7"), sut.get("payload-oxum"));
  }
}
