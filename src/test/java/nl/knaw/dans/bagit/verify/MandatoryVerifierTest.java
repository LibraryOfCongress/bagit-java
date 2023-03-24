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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.exceptions.FileNotInPayloadDirectoryException;
import nl.knaw.dans.bagit.exceptions.MissingPayloadDirectoryException;
import nl.knaw.dans.bagit.exceptions.MissingPayloadManifestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.exceptions.MissingBagitFileException;
import nl.knaw.dans.bagit.reader.BagReader;

public class MandatoryVerifierTest extends PrivateConstructorTest {
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  private BagReader reader = new BagReader();

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MandatoryVerifier.class);
  }
  
  @Test
  public void testErrorWhenFetchItemsDontExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bad-fetch-bag").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInPayloadDirectoryException.class,
        () -> { MandatoryVerifier.checkFetchItemsExist(bag.getItemsToFetch(), bag.getRootDir()); });
  }
  
  @Test
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(folder);
    Path dataDir = createDirectory("data");
    deleteDirectory(dataDir);
    
    Assertions.assertThrows(MissingPayloadDirectoryException.class,
        () -> { MandatoryVerifier.checkPayloadDirectoryExists(bag); });
  }
  
  @Test
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(folder);
    Path manifestFile = folder.resolve("manifest-md5.txt");
    Files.delete(manifestFile);
    
    Assertions.assertThrows(MissingPayloadManifestException.class,
        () -> { MandatoryVerifier.checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion()); });
  }
  
  @Test
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(folder);
    Path bagitFile = folder.resolve("bagit.txt");
    Files.delete(bagitFile);
    
    Assertions.assertThrows(MissingBagitFileException.class, 
        () -> { MandatoryVerifier.checkBagitFileExists(bag.getRootDir(), bag.getVersion()); });
  }
  
  private void copyBagToTestFolder() throws Exception{
    Files.walk(rootDir).forEach(path ->{
      try {
          Files.copy(path, Paths.get(path.toString().replace(
              rootDir.toString(),
              folder.toString())));
      } catch (Exception e) {}});
  }
  
  private void deleteDirectory(Path directory) throws Exception{
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    });
  }
}
