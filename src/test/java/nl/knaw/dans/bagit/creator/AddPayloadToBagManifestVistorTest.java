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
package nl.knaw.dans.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import nl.knaw.dans.bagit.TempFolderTest;
import nl.knaw.dans.bagit.TestUtils;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddPayloadToBagManifestVistorTest extends TempFolderTest {

  @Test
  public void includeDotKeepFilesInManifest() throws Exception{
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    Map<Manifest, MessageDigest> map = new HashMap<>();
    map.put(manifest, messageDigest);
    boolean includeHiddenFiles = false;
    Path start = Paths.get(new File("src/test/resources/dotKeepExampleBag").toURI()).resolve("data");
    
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(map, includeHiddenFiles);
    Files.walkFileTree(start, sut);
    
    Assertions.assertEquals(1, manifest.getFileToChecksumMap().size());
    Assertions.assertTrue(manifest.getFileToChecksumMap().containsKey(start.resolve("fooDir/.keep")));
  }
  
  @Test
  public void testSkipDotBagitDir() throws IOException{
    Path dotBagitDirectory = createDirectory(".bagit");
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, true);
    FileVisitResult returned = sut.preVisitDirectory(dotBagitDirectory, null);
    Assertions.assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testSkipHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, false);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    Assertions.assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testIncludeHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, true);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    Assertions.assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  @Test
  public void testSkipHiddenFile() throws IOException{
    Path hiddenFile = createHiddenFile();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, false);
    FileVisitResult returned = sut.visitFile(hiddenFile, null);
    Assertions.assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  private Path createHiddenDirectory() throws IOException{
    Path hiddenDirectory = createDirectory(".someHiddenDir");
    
    if(TestUtils.isExecutingOnWindows()){
      Files.setAttribute(hiddenDirectory, "dos:hidden", Boolean.TRUE);
    }
    
    return hiddenDirectory;
  }
  
  private Path createHiddenFile() throws IOException{
    Path hiddenDirectory = createFile(".someHiddenFile");
    
    if(TestUtils.isExecutingOnWindows()){
      Files.setAttribute(hiddenDirectory, "dos:hidden", Boolean.TRUE);
    }
    
    return hiddenDirectory;
  }
}
