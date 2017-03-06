package gov.loc.repository.bagit.domain;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetadataTest extends Assert {
  private Metadata sut;
  
  @Before
  public void setup(){
    sut = new Metadata();
  }

  @Test
  public void testPayloadOxumUpsert(){
    assertNull(sut.get("payload-oxum"));
    
    //test inserting
    String payloadOxum = "6.1";
    sut.upsertPayloadOxum(payloadOxum);
    assertEquals(Arrays.asList(payloadOxum), sut.get("payload-oxum"));
    
    payloadOxum = "25.2";
    sut.upsertPayloadOxum(payloadOxum);
    assertEquals(Arrays.asList(payloadOxum), sut.get("payload-oxum"));
  }
  
  @Test
  public void testCaseInsensitiveAccess(){
    sut.add("key", "value");
    
    assertEquals(Arrays.asList("value"), sut.get("KEY"));
    assertEquals(Arrays.asList("value"), sut.get("key"));
    assertEquals(Arrays.asList("value"), sut.get("Key"));
    assertEquals(Arrays.asList("value"), sut.get("kEY"));
  }
  
  @Test
  public void testCaseIsPreserved(){
    String key = "FoOVaLuE";
    String value = "BaRVaLuE";
    sut.add(key, value);
    
    assertEquals(key, sut.getAll().get(0).getKey());
    assertEquals(value, sut.getAll().get(0).getValue());
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
    
    assertTrue("should be same since same memory reference", sut.equals(sut));
    assertFalse("should not be null", sut.equals(null));
    assertFalse("should not equal each other since they are different types", sut.equals("a string"));
    assertFalse("should not equal each other since they have different values", sut.equals(differentValues));
    assertFalse("should not equal each other since one has duplicate values", sut.equals(repeatedValues));
    assertTrue("should be equal since they hold the same values", sut.equals(same));
  }
  
  @Test
  public void testRemove(){
    sut.add("key", "value");
    sut.add("key", "value");
    sut.add("key", "value");
    
    assertEquals(3, sut.getList().size());
    assertEquals(3, sut.getMap().get("KEY").size());
    assertEquals(3, sut.getAll().size());
    
    sut.remove("key");
    assertEquals(0, sut.getList().size());
    assertNull(sut.getMap().get("KEY"));
    assertEquals(0, sut.getAll().size());
  }
  
  @Test
  public void testAddingMultiplePayloadOxumJustUpsertsInstead(){
    sut.add("Payload-Oxum", "16.1");
    sut.add("Payload-Oxum", "20.5");
    sut.add("Payload-Oxum", "100.7");
    
    assertEquals(Arrays.asList("100.7"), sut.get("payload-oxum"));
  }
}
