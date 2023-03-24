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

import java.nio.file.Path;
import java.nio.file.Paths;

import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TagFileReaderTest {

  @Test
  public void testCreateFileFromManifest() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Path path = TagFileReader.createFileFromManifest(bagRootDir, "data/bar/ham.txt");
    Assertions.assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testCreateFileFromManifestWithAsterisk() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Path path = TagFileReader.createFileFromManifest(bagRootDir, "*data/bar/ham.txt");
    Assertions.assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testCreateFileFromManifestWithURISyntax() throws Exception{
    Path bagRootDir = Paths.get("/foo");
    String uri = "file:///foo/data/bar/ham.txt";
    Path path = TagFileReader.createFileFromManifest(bagRootDir, uri);
    Assertions.assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testBackslashThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(InvalidBagitFileFormatException.class,
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "data\\bar\\ham.txt"); });
  }
  
  @Test
  public void testOutsideDataDirReferenceThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(MaliciousPathException.class,
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "/bar/ham.txt"); });
  }
  
  @Test
  public void testRelativePathOutsideDataDirThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "../bar/ham.txt"); });
  }
  
  @Test
  public void testHomeDirReferenceThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "~/bar/ham.txt"); });
  }
  
  @Test
  public void testBadURIThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { TagFileReader.createFileFromManifest(bagRootDir, "file://C:/foo^"); });
  }
}
