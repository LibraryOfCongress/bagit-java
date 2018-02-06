package gov.loc.repository.bagit.conformance;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.conformance.BagitVersionIsNotAcceptableException;
import gov.loc.repository.bagit.exceptions.conformance.FetchFileNotAllowedException;
import gov.loc.repository.bagit.exceptions.conformance.MetatdataValueIsNotAcceptableException;
import gov.loc.repository.bagit.exceptions.conformance.MetatdataValueIsNotRepeatableException;
import gov.loc.repository.bagit.exceptions.conformance.RequiredManifestNotPresentException;
import gov.loc.repository.bagit.exceptions.conformance.RequiredMetadataFieldNotPresentException;
import gov.loc.repository.bagit.exceptions.conformance.RequiredTagFileNotPresentException;
import gov.loc.repository.bagit.reader.BagReader;

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
  
  @Test(expected=FetchFileNotAllowedException.class)
  public void testFetchFileNotAllowedException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/failFetchBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=RequiredMetadataFieldNotPresentException.class)
  public void testRequiredMetadataFieldNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/emailFieldMissingBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=MetatdataValueIsNotAcceptableException.class)
  public void testMetatdataValueIsNotAcceptableException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/wrongValueForContactNameBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=MetatdataValueIsNotRepeatableException.class)
  public void testMetadataValueIsNotRepeatableException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/repeatedMetadataBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=RequiredManifestNotPresentException.class)
  public void testRequiredPayloadManifestNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/missingRequiredPayloadManifestBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=RequiredManifestNotPresentException.class)
  public void testRequiredTagManifestNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/missingRequiredTagManifestBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=RequiredTagFileNotPresentException.class)
  public void testRequiredTagFileNotPresentException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/missingRequiredTagFileBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
  
  @Test(expected=BagitVersionIsNotAcceptableException.class)
  public void testBagitVersionIsNotAcceptableException() throws Exception{
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/wrongBagitVersionBag").toPath();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagProfileChecker.bagConformsToProfile(inputStream, bag);
    }
  }
}
