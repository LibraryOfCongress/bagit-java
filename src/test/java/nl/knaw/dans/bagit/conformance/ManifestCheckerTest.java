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

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.domain.Version;

public class ManifestCheckerTest extends PrivateConstructorTest {
  
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(ManifestChecker.class);
  }
  
  @Test
  public void testCheckManifests() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Collections.emptyList());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM));
    Assertions.assertTrue(warnings.contains(BagitWarning.DIFFERENT_CASE));
    if(FileSystems.getDefault().getClass().getName() != "sun.nio.fs.MacOSXFileSystem"){ //don't test normalization on mac
      Assertions.assertTrue(warnings.contains(BagitWarning.DIFFERENT_NORMALIZATION));
    }
    Assertions.assertTrue(warnings.contains(BagitWarning.BAG_WITHIN_A_BAG));
    Assertions.assertTrue(warnings.contains(BagitWarning.LEADING_DOT_SLASH));
    Assertions.assertTrue(warnings.contains(BagitWarning.NON_STANDARD_ALGORITHM));
    Assertions.assertTrue(warnings.contains(BagitWarning.OS_SPECIFIC_FILES));
    Assertions.assertTrue(warnings.contains(BagitWarning.MISSING_TAG_MANIFEST));
  }
  
  @Test
  public void testPayloadManifestSetsShouldBeSame() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();
    Path manifestPath = Paths.get("src","test","resources","payloadManifestsDiffer");
    ManifestChecker.checkManifests(new Version(1,0), manifestPath, StandardCharsets.UTF_8, warnings, Collections.emptyList());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.MANIFEST_SETS_DIFFER));
  }
  
  @Test
  public void testTagManifestSetsShouldBeSame() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();
    Path manifestPath = Paths.get("src","test","resources","payloadManifestsDiffer");
    ManifestChecker.checkManifests(new Version(1,0), manifestPath, StandardCharsets.UTF_8, warnings, Collections.emptyList());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.MANIFEST_SETS_DIFFER));
  }
  
  @Test
  public void testCheckTagManifest() throws Exception{
    createFile("tagmanifest-md5.txt");
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), folder, StandardCharsets.UTF_16, warnings, Collections.emptyList());
    Assertions.assertFalse(warnings.contains(BagitWarning.MISSING_TAG_MANIFEST));
  }
  
  @Test
  public void testLinterIgnoreWeakChecksum() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.WEAK_CHECKSUM_ALGORITHM));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM));
  }
  
  @Test
  public void testLinterIgnoreCase() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.DIFFERENT_CASE));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.DIFFERENT_CASE));
  }
  
  @Test
  public void testLinterNormalization() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.DIFFERENT_NORMALIZATION));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.DIFFERENT_NORMALIZATION));
  }
  
  @Test
  public void testLinterIgnoreBagWithinABag() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.BAG_WITHIN_A_BAG));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.BAG_WITHIN_A_BAG));
  }
  
  @Test
  public void testLinterIgnoreRelativePath() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.LEADING_DOT_SLASH));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.LEADING_DOT_SLASH));
  }
  
  @Test
  public void testLinterIgnoreNonStandardChecksumAlgorithm() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.NON_STANDARD_ALGORITHM));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.NON_STANDARD_ALGORITHM));
  }
  
  @Test
  public void testLinterIgnoreOSSpecificFiles() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();

    ManifestChecker.checkManifests(Version.LATEST_BAGIT_VERSION(), rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.OS_SPECIFIC_FILES));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.OS_SPECIFIC_FILES));
  }

  @Test
  public void testOSSpecificFilesRegex(){
    String regex = ManifestChecker.getOsFilesRegex();
    String[] osFilesToTest = new String[]{"data/Thumbs.db", "data/.DS_Store", "data/.Spotlight-V100", "data/.Trashes", 
        "data/._.Trashes", "data/.fseventsd"};
    
    for(String osFileToTest : osFilesToTest){
      Assertions.assertTrue(osFileToTest.matches(regex), osFileToTest + " should match regex but it doesn't");
    }
  }
  
  @Test
  public void testParsePath() throws InvalidBagitFileFormatException {
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { ManifestChecker.parsePath("foobarham"); });
  }
  
  @Test
  public void testCheckAlgorthm(){
    Set<BagitWarning> warnings;
    String[] algorithms = new String[]{"md5", "sha", "sha1", "sha224", "sha256", "sha384"};
    for(String algorithm : algorithms){
     warnings = new HashSet<>();
     ManifestChecker.checkAlgorthm(algorithm, warnings, Collections.emptyList());
       Assertions.assertTrue(warnings.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM));
    }
  }
}
