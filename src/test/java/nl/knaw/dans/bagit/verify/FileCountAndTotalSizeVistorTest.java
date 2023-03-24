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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.TestUtils;

/**
 * Tests the ignore of hidden files while walking the file tree. 
 */
public class FileCountAndTotalSizeVistorTest {
  
  private Path payloadDir = Paths.get(new File("src/test/resources/hiddenFoldersAndFiles").toURI());
  
  @BeforeEach
  public void setup() throws IOException{
    TestUtils.makeFilesHiddenOnWindows(payloadDir);
  }
  
  @Test
  public void testGeneratePayloadOxum() throws IOException{
    final FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor();
    Files.walkFileTree(payloadDir, vistor);
    Assertions.assertEquals(5, vistor.getCount());
    Assertions.assertEquals(101, vistor.getTotalSize());
  }
}
