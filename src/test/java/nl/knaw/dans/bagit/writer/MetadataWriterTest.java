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

import nl.knaw.dans.bagit.PrivateConstructorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.domain.Metadata;
import nl.knaw.dans.bagit.domain.Version;

public class MetadataWriterTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MetadataWriter.class);
  }
  
  @Test
  public void testWriteBagitInfoFile() throws IOException{
    Path rootDir = createDirectory("writeBagitInfo");
    Path bagInfo = rootDir.resolve("bag-info.txt");
    Path packageInfo = rootDir.resolve("package-info.txt");
    Metadata metadata = new Metadata();
    metadata.add("key1", "value1");
    metadata.add("key2", "value2");
    metadata.add("key3", "value3");
    
    Assertions.assertFalse(Files.exists(bagInfo));
    Assertions.assertFalse(Files.exists(packageInfo));
    
    MetadataWriter.writeBagMetadata(metadata, new Version(0,96), rootDir, StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(bagInfo));
    
    MetadataWriter.writeBagMetadata(metadata, new Version(0,95), rootDir, StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(packageInfo));
  }
}
