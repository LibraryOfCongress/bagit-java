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
package nl.knaw.dans.bagit.verify;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.exceptions.InvalidPayloadOxumException;
import nl.knaw.dans.bagit.exceptions.PayloadOxumDoesNotExistException;
import nl.knaw.dans.bagit.reader.BagReader;

public class QuickVerifierTest extends PrivateConstructorTest {

  private BagReader reader = new BagReader();
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(QuickVerifier.class);
  }
  
  @Test
  public void testCanQuickVerify() throws Exception{
    Bag bag = reader.read(rootDir);
    boolean canQuickVerify = QuickVerifier.canQuickVerify(bag);
    Assertions.assertFalse(canQuickVerify,
        "Since " + bag.getRootDir() + " DOES NOT contain the metadata Payload-Oxum then it should return false!");
    
    Path passingRootDir = Paths.get(new File("src/test/resources/bags/v0_94/bag").toURI());
    bag = reader.read(passingRootDir);
    canQuickVerify = QuickVerifier.canQuickVerify(bag);
    Assertions.assertTrue(canQuickVerify,
        "Since " + bag.getRootDir() + " DOES contain the metadata Payload-Oxum then it should return true!");
  }
  
  @Test 
  public void testQuickVerify() throws Exception{
    Path passingRootDir = Paths.get(new File("src/test/resources/bags/v0_94/bag").toURI());
    Bag bag = reader.read(passingRootDir);
    
    QuickVerifier.quicklyVerify(bag);
  }
  
  @Test
  public void testExceptionIsThrownWhenPayloadOxumDoesntExist() throws Exception{
    Bag bag = reader.read(rootDir);
    Assertions.assertThrows(PayloadOxumDoesNotExistException.class, 
        () -> { QuickVerifier.quicklyVerify(bag); });
  }
  
  @Test
  public void testInvalidByteSizeForQuickVerify() throws Exception{
    Path badRootDir = Paths.get(new File("src/test/resources/badPayloadOxumByteSize/bag").toURI());
    Bag bag = reader.read(badRootDir);
    
    Assertions.assertThrows(InvalidPayloadOxumException.class, 
        () -> { QuickVerifier.quicklyVerify(bag); });
  }
  
  @Test
  public void testInvalidFileCountForQuickVerify() throws Exception{
    Path badRootDir = Paths.get(new File("src/test/resources/badPayloadOxumFileCount/bag").toURI());
    Bag bag = reader.read(badRootDir);
    
    Assertions.assertThrows(InvalidPayloadOxumException.class, 
        () -> { QuickVerifier.quicklyVerify(bag); });
  }
}
