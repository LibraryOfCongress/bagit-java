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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.exceptions.FileNotInManifestException;
import nl.knaw.dans.bagit.exceptions.FileNotInPayloadDirectoryException;
import nl.knaw.dans.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import nl.knaw.dans.bagit.reader.BagReader;

public class ManifestVerifierTest {
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  private BagReader reader = new BagReader();
  
  private ManifestVerifier sut;
  
  @BeforeEach
  public void setup(){
    sut = new ManifestVerifier(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  @Test
  public void testOtherConstructors() throws Exception {
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut = new ManifestVerifier();
    sut.verifyManifests(bag, true);
    
    sut = new ManifestVerifier(Executors.newCachedThreadPool());
    sut.verifyManifests(bag, true);
  }

  @Test
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInManifestDontExist").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInPayloadDirectoryException.class, 
        () -> { sut.verifyManifests(bag, true); });
  }
  
  @Test
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInPayloadDirAreNotInManifest").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.verifyManifests(bag, true); });
  }
  
  @Test
  public void testBagWithTagFilesInPayloadIsValid() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.verifyManifests(bag, true);
  }
  
  @Test
  public void testNotALlFilesListedInAllManifestsThrowsException() throws Exception{
    Path bagDir = Paths.get(new File("src/test/resources/notAllFilesListedInAllManifestsBag").toURI());
    Bag bag = reader.read(bagDir);
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.verifyManifests(bag, true); });
  }
}
