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
package nl.knaw.dans.bagit.reader;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;

public class ManifestReaderTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(ManifestReader.class);
  }
  
  @Test
  public void testReadAllManifests() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = new Bag();
    bag.setRootDir(rootBag);
    ManifestReader.readAllManifests(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping(), rootBag, bag);
    Assertions.assertEquals(1, bag.getPayLoadManifests().size());
    Assertions.assertEquals(1, bag.getTagManifests().size());
  }
  
  @Test
  public void testReadUpDirectoryMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/upAdirectoryReference.txt").toURI());
    Assertions.assertThrows(MaliciousPathException.class,
        () -> { ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8); });
  }
  
  @Test
  public void testReadTildeMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/tildeReference.txt").toURI());
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8); });
  }
  
  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void testReadFileUrlMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/fileUrl.txt").toURI());
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> {  ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/bar"), StandardCharsets.UTF_8); });
  }
  
  @Test
  public void testReadWindowsSpecialDirMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/windowsSpecialDirectoryName.txt").toURI());
    Assertions.assertThrows(InvalidBagitFileFormatException.class,
        () -> { ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8); });
  }
}
