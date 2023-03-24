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
package nl.knaw.dans.bagit.conformance;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.exceptions.conformance.BagitVersionIsNotAcceptableException;
import nl.knaw.dans.bagit.exceptions.conformance.FetchFileNotAllowedException;
import nl.knaw.dans.bagit.exceptions.conformance.MetatdataValueIsNotAcceptableException;
import nl.knaw.dans.bagit.exceptions.conformance.MetatdataValueIsNotRepeatableException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredManifestNotPresentException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredMetadataFieldNotPresentException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredTagFileNotPresentException;
import nl.knaw.dans.bagit.reader.BagReader;

public class BagProfileCheckerTest extends PrivateConstructorTest {
  private static final Path profileJson = new File("src/test/resources/bagitProfiles/exampleProfile.json").toPath();
  private static final BagReader reader = new BagReader();
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagProfileChecker.class);
  }
  
  @Test
  public void testBagConformsToProfile() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/profileConformantBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test
  public void testFetchFileNotAllowedException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/failFetchBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(FetchFileNotAllowedException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); }); 
    }
  }
  
  @Test
  public void testRequiredMetadataFieldNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/emailFieldMissingBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(RequiredMetadataFieldNotPresentException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
  
  @Test
  public void testMetatdataValueIsNotAcceptableException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/wrongValueForContactNameBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(MetatdataValueIsNotAcceptableException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
  
  @Test
  public void testMetadataValueIsNotRepeatableException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/repeatedMetadataBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(MetatdataValueIsNotRepeatableException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
  
  @Test
  public void testRequiredPayloadManifestNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/missingRequiredPayloadManifestBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(RequiredManifestNotPresentException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
  
  @Test
  public void testRequiredTagManifestNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/missingRequiredTagManifestBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(RequiredManifestNotPresentException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
  
  @Test
  public void testRequiredTagFileNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/missingRequiredTagFileBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(RequiredTagFileNotPresentException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
  
  @Test
  public void testBagitVersionIsNotAcceptableException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/wrongBagitVersionBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      Assertions.assertThrows(BagitVersionIsNotAcceptableException.class, 
          () -> { BagProfileChecker.bagConformsToProfile(inputStream, bag); });
    }
  }
}
