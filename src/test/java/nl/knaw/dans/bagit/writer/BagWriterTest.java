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

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.creator.BagCreator;
import nl.knaw.dans.bagit.reader.BagReader;

public class BagWriterTest extends PrivateConstructorTest {
  
  private BagReader reader = new BagReader();
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagWriter.class);
  }
  
  @Test
  public void testGetCorrectRelativeOuputPath() throws Exception{
    Path root = createDirectory("newFolder");
    Bag bag = BagCreator.bagInPlace(root, Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    
    Path testFile = root.resolve("data").resolve("fooFile.txt");
    Files.createFile(testFile);
    Manifest manifest = (Manifest) bag.getPayLoadManifests().toArray()[0];
    manifest.getFileToChecksumMap().put(testFile, "CHECKSUM");
    bag.getPayLoadManifests().add(manifest);
    
    Path newRoot = createDirectory("newRoot");
    BagWriter.write(bag, newRoot);
    Assertions.assertTrue(Files.exists(newRoot.resolve("data").resolve("fooFile.txt")));
  }
  
  @Test
  public void testWriteVersion95() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_95/bag").toURI());
    Bag bag = reader.read(rootDir); 
    Path bagitDirPath = createDirectory("version95");
    List<Path> expectedPaths = Arrays.asList(bagitDirPath.resolve("tagmanifest-md5.txt"),
        bagitDirPath.resolve("manifest-md5.txt"),
        bagitDirPath.resolve("bagit.txt"),
        bagitDirPath.resolve("package-info.txt"),
        bagitDirPath.resolve("data"),
        bagitDirPath.resolve("data").resolve("test1.txt"),
        bagitDirPath.resolve("data").resolve("test2.txt"),
        bagitDirPath.resolve("data").resolve("dir1"),
        bagitDirPath.resolve("data").resolve("dir2"), 
        bagitDirPath.resolve("data").resolve("dir1").resolve("test3.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("test4.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3").resolve("test5.txt"));
    
    BagWriter.write(bag, bagitDirPath);
    for(Path expectedPath : expectedPaths){
      Assertions.assertTrue(Files.exists(expectedPath), "Expected " + expectedPath + " to exist!");
    }
  }
  
  @Test
  public void testWriteVersion97() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = reader.read(rootDir); 
    Path bagitDirPath = createDirectory("version97");
    List<Path> expectedPaths = Arrays.asList(bagitDirPath.resolve("tagmanifest-md5.txt"),
        bagitDirPath.resolve("manifest-md5.txt"),
        bagitDirPath.resolve("bagit.txt"),
        bagitDirPath.resolve("bag-info.txt"),
        bagitDirPath.resolve("data"),
        bagitDirPath.resolve("data").resolve("test1.txt"),
        bagitDirPath.resolve("data").resolve("test2.txt"),
        bagitDirPath.resolve("data").resolve("dir1"),
        bagitDirPath.resolve("data").resolve("dir2"), 
        bagitDirPath.resolve("data").resolve("dir1").resolve("test3.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("test4.txt"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3"),
        bagitDirPath.resolve("data").resolve("dir2").resolve("dir3").resolve("test5.txt"),
        bagitDirPath.resolve("addl_tags"),
        bagitDirPath.resolve("addl_tags").resolve("tag1.txt"));
    
    BagWriter.write(bag, bagitDirPath);
    for(Path expectedPath : expectedPaths){
      Assertions.assertTrue(Files.exists(expectedPath), "Expected " + expectedPath + " to exist!");
    }
  }
  
  @Test
  public void testWriteVersion2_0() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v2_0/bag").toURI());
    Bag bag = reader.read(rootDir);
    
    Path bagitDirPath = createDirectory("version2");
    List<Path> expectedPaths = Arrays.asList(bagitDirPath.resolve(".bagit"),
        bagitDirPath.resolve(".bagit").resolve("manifest-md5.txt"),
        bagitDirPath.resolve(".bagit").resolve("bagit.txt"),
        bagitDirPath.resolve(".bagit").resolve("bag-info.txt"),
        bagitDirPath.resolve(".bagit").resolve("tagmanifest-md5.txt"),
        bagitDirPath.resolve("test1.txt"),
        bagitDirPath.resolve("test2.txt"),
        bagitDirPath.resolve("dir1"),
        bagitDirPath.resolve("dir2"), 
        bagitDirPath.resolve("dir1").resolve("test3.txt"),
        bagitDirPath.resolve("dir2").resolve("test4.txt"),
        bagitDirPath.resolve("dir2").resolve("dir3"),
        bagitDirPath.resolve("dir2").resolve("dir3").resolve("test5.txt"));
    
    BagWriter.write(bag, bagitDirPath);
    for(Path expectedPath : expectedPaths){
      Assertions.assertTrue(Files.exists(expectedPath), "Expected " + expectedPath + " to exist!");
    }
  }
  
  @Test
  public void testWriteHoley() throws Exception{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_96/holey-bag").toURI());
    Bag bag = reader.read(rootDir); 
    Path bagitDir = createDirectory("holyBag");
    
    BagWriter.write(bag, bagitDir);
    Assertions.assertTrue(Files.exists(bagitDir));
    
    Path fetchFile = bagitDir.resolve("fetch.txt");
    Assertions.assertTrue(Files.exists(fetchFile));
  }
  
  @Test
  public void testWriteEmptyBagStillCreatesDataDir() throws Exception{
    Bag bag = new Bag();
    bag.setRootDir(createDirectory("emptyBag"));
    Path dataDir = bag.getRootDir().resolve("data");
    
    BagWriter.write(bag, bag.getRootDir());
    Assertions.assertTrue(Files.exists(dataDir));
  }
}
