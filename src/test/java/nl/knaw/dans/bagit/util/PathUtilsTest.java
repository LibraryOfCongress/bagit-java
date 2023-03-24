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
package nl.knaw.dans.bagit.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.knaw.dans.bagit.domain.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.TestUtils;
import nl.knaw.dans.bagit.domain.Version;

public class PathUtilsTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(PathUtils.class);
  }

  @Test
  public void testDecode(){
    //just carriage return 
    String testString = "/foo/bar/ham%0D";
    String expectedDecoded = "/foo/bar/ham\r";
    String actualDecoded = PathUtils.decodeFilname(testString);
    Assertions.assertEquals(expectedDecoded, actualDecoded);
    
    //just new line
    testString = "/foo/bar/ham%0A";
    expectedDecoded = "/foo/bar/ham\n";
    actualDecoded = PathUtils.decodeFilname(testString);
    Assertions.assertEquals(expectedDecoded, actualDecoded);
    
    //both carriage return and new line
    testString = "/foo/bar/ham%0D%0A";
    expectedDecoded = "/foo/bar/ham\r\n";
    actualDecoded = PathUtils.decodeFilname(testString);
    Assertions.assertEquals(expectedDecoded, actualDecoded);
  }
  
  @Test
  public void testEncode(){
    if(!TestUtils.isExecutingOnWindows()){
      //just carriage return
      Path testPath = Paths.get("foo/bar/ham\r");
      String expectedEncoded = "foo/bar/ham%0D";
      String actualEncoded = PathUtils.encodeFilename(testPath);
      Assertions.assertEquals(expectedEncoded, actualEncoded);
      
      //just new line
      testPath = Paths.get("foo/bar/ham\n");
      expectedEncoded = "foo/bar/ham%0A";
      actualEncoded = PathUtils.encodeFilename(testPath);
      Assertions.assertEquals(expectedEncoded, actualEncoded);
      
      //both carriage return and new line
      testPath = Paths.get("foo/bar/ham\r\n");
      expectedEncoded = "foo/bar/ham%0D%0A";
      actualEncoded = PathUtils.encodeFilename(testPath);
      Assertions.assertEquals(expectedEncoded, actualEncoded);
    }
  }
  
  @Test
  public void testGetDataDirUsingBag() throws IOException{
    Bag bag = new Bag(new Version(2,0));
    bag.setRootDir(Paths.get("foo"));
    
    Path expectedPath = bag.getRootDir();
    Path actualPath = PathUtils.getDataDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    bag = new Bag(new Version(0, 97));
    bag.setRootDir(Paths.get("foo"));
    
    expectedPath = bag.getRootDir().resolve("data");
    actualPath = PathUtils.getDataDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGetDataDirUsingVersion() throws IOException{
    Path input = Paths.get("foo");
    Path expectedPath = input;
    Path actualPath = PathUtils.getDataDir(new Version(2,0), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    expectedPath = input.resolve("data");
    actualPath = PathUtils.getDataDir(new Version(0, 97), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGetBagitDirUsingBag(){
    Bag bag = new Bag(new Version(2,0));
    bag.setRootDir(Paths.get("foo"));
    
    Path expectedPath = bag.getRootDir().resolve(".bagit");
    Path actualPath = PathUtils.getBagitDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    bag = new Bag(new Version(0, 97));
    bag.setRootDir(Paths.get("foo"));
    
    expectedPath = bag.getRootDir();
    actualPath = PathUtils.getBagitDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGetBagitDirUsingVersion() throws IOException{
    Path input = Paths.get("foo");
    Path expectedPath = input.resolve(".bagit");
    Path actualPath = PathUtils.getBagitDir(new Version(2,0), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    expectedPath = input;
    actualPath = PathUtils.getBagitDir(new Version(0, 97), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGeneratePayloadOxum() throws IOException{
    Path testPath = Paths.get("src", "test", "resources", "bags", "v0_97", "bag", "data");
    Assertions.assertEquals("25.5", PathUtils.generatePayloadOxum(testPath));
  }
}
