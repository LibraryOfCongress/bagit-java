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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.FetchItem;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PayloadWriterTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(PayloadWriter.class);
  }
  
  @Test
  public void testWritePayloadFiles() throws IOException, URISyntaxException{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Path testFile = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").toURI());
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    Path outputDir = createDirectory("writePayloadFiles");
    Path copiedFile = outputDir.resolve("data/dir1/test3.txt");
    
    Assertions.assertFalse(Files.exists(copiedFile) || Files.exists(copiedFile.getParent()));
    PayloadWriter.writePayloadFiles(payloadManifests, new ArrayList<>(), outputDir, rootDir);
    Assertions.assertTrue(Files.exists(copiedFile) || Files.exists(copiedFile.getParent()));
  }
  
  @Test
  public void testWritePayloadFilesMinusFetchFiles() throws IOException, URISyntaxException{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Path testFile = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").toURI());
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    Path outputDir = createDirectory("writePayloadWithoutFetch");
    Path copiedFile = outputDir.resolve("data/dir1/test3.txt");
    
    Assertions.assertFalse(Files.exists(copiedFile) || Files.exists(copiedFile.getParent()));
    PayloadWriter.writePayloadFiles(payloadManifests,
        Arrays.asList(new FetchItem(null, null, Paths.get("data/dir1/test3.txt"))),
          outputDir,
          rootDir.resolve("data"));
    Assertions.assertFalse(Files.exists(copiedFile) || Files.exists(copiedFile.getParent()));
  }
}
