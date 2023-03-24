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
package nl.knaw.dans.bagit.writer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ManifestWriterTest extends PrivateConstructorTest {

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(ManifestWriter.class);
  }
  
  @Test
  public void testWriteTagManifests() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(Paths.get("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    Path outputDir = createDirectory("tagManifests");
    Path tagManifest = outputDir.resolve("tagmanifest-md5.txt");
    
    Assertions.assertFalse(Files.exists(tagManifest));
    ManifestWriter.writeTagManifests(tagManifests, outputDir, Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(tagManifest));
  }
  
  @Test
  public void testManifestsDontContainWindowsFilePathSeparator() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(Paths.get("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    Path outputDir = createDirectory("noWindowsPathSeparator");
    Path tagManifest = outputDir.resolve("tagmanifest-md5.txt");
    
    Assertions.assertFalse(Files.exists(tagManifest));
    ManifestWriter.writeTagManifests(tagManifests, outputDir, Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8);
    
    List<String> lines = Files.readAllLines(tagManifest);
    for(String line : lines){
      Assertions.assertFalse(line.contains("\\"), 
          "Line [" + line + "] contains \\ which is not allowed by the bagit specification");
    }
  }
}
