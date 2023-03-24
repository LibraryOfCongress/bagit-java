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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.knaw.dans.bagit.TempFolderTest;
import nl.knaw.dans.bagit.TestUtils;
import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.hash.StandardSupportedAlgorithms;
import nl.knaw.dans.bagit.util.PathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.domain.Version;

public class BagCreatorTest extends TempFolderTest {
  
  @Test
  public void testBagInPlaceWithFileNamedData() throws IOException, NoSuchAlgorithmException{
    Path testFolder = createDirectory("someFolder");
    Path dataFile = testFolder.resolve("data");
    Files.createFile(dataFile);
    
    BagCreator.bagInPlace(testFolder, Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    Assertions.assertTrue(Files.exists(testFolder.resolve("data").resolve("data")));
  }
  
  @Test
  public void testBagInPlace() throws IOException, NoSuchAlgorithmException{
    TestStructure structure = createTestStructure();
    
    Bag bag = BagCreator.bagInPlace(folder, Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    
    Assertions.assertEquals(Version.LATEST_BAGIT_VERSION(), bag.getVersion());
    
    Path expectedManifest = folder.resolve("manifest-md5.txt");
    Assertions.assertTrue(Files.exists(expectedManifest));
    
    Path expectedTagManifest = folder.resolve("tagmanifest-md5.txt");
    Assertions.assertTrue(Files.exists(expectedTagManifest));
    
    Path bagitFile = folder.resolve("bagit.txt");
    Assertions.assertTrue(Files.exists(bagitFile));
    
    //make sure the hidden folder was not included in the data directory
    File hiddenFolder = new File(bag.getRootDir().resolve("data").toFile(), ".hiddenFolder");
    Assertions.assertFalse(hiddenFolder.exists());
    
    for(Manifest manifest : bag.getPayLoadManifests()){
      for(Path expectedPayloadFile : manifest.getFileToChecksumMap().keySet()){
        Assertions.assertTrue(structure.regularPayloadFiles.contains(expectedPayloadFile));
      }
    }
  }
  
  @Test
  public void testBagInPlaceIncludingHidden() throws IOException, NoSuchAlgorithmException{
    TestStructure structure = createTestStructure();
    
    Bag bag = BagCreator.bagInPlace(folder, Arrays.asList(StandardSupportedAlgorithms.MD5), true);
    
    Assertions.assertEquals(Version.LATEST_BAGIT_VERSION(), bag.getVersion());
    
    Path expectedManifest = folder.resolve("manifest-md5.txt");
    Assertions.assertTrue(Files.exists(expectedManifest));
    
    Path expectedTagManifest = folder.resolve("tagmanifest-md5.txt");
    Assertions.assertTrue(Files.exists(expectedTagManifest));
    
    Path bagitFile = folder.resolve("bagit.txt");
    Assertions.assertTrue(Files.exists(bagitFile));
    
    for(Manifest manifest : bag.getPayLoadManifests()){
      for(Path expectedPayloadFile : manifest.getFileToChecksumMap().keySet()){
        Assertions.assertTrue(structure.regularPayloadFiles.contains(expectedPayloadFile) || 
            structure.hiddenPayloadFiles.contains(expectedPayloadFile),
            expectedPayloadFile + " doesn't exist but it should!");
      }
    }
  }
  
  private TestStructure createTestStructure() throws IOException{
    TestStructure structure = new TestStructure();
    
    Path dataDir = createDirectory("data");
    
    Path file1 = createFile("file1.txt");
    createDirectory("folder1");
    Path file2 = createFile("file2.txt");
    
    Path hiddenFile = createFile(".hiddentFile.txt");
    Path hiddenDirectory = createDirectory(".hiddenFolder");
    
    TestUtils.makeFilesHiddenOnWindows(folder);
    
    Assertions.assertTrue(Files.isHidden(hiddenFile));
    //because the Files.isHidden() always returns false for windows if it is a directory
    Assertions.assertTrue(PathUtils.isHidden(hiddenDirectory));
    
    Path hiddenFile2 = hiddenDirectory.resolve(".hiddenFile2.txt");
    Files.createFile(hiddenFile2);
    Path file3 = hiddenDirectory.resolve("file3.txt");
    Files.createFile(file3);
    
    structure.regularPayloadFiles.add(dataDir.resolve(file1.getFileName()));
    structure.regularPayloadFiles.add(dataDir.resolve(file2.getFileName()));
    
    structure.hiddenPayloadFiles.add(dataDir.resolve(hiddenFile.getFileName()));
    Path hiddenDirPath = dataDir.resolve(hiddenDirectory.getFileName());
    Path hiddenFile2Path = hiddenDirPath.resolve(hiddenFile2.getFileName());
    structure.hiddenPayloadFiles.add(hiddenFile2Path);
    structure.hiddenPayloadFiles.add(hiddenDirPath.resolve(file3.getFileName()));
    return structure;
  }
  
  @Test
  public void testCreateDotBagit() throws IOException, NoSuchAlgorithmException{
    createTestStructure();
    
    Path dotbagitDir = createDirectory(".bagit");
    Path expectedManifestFile = dotbagitDir.resolve("manifest-md5.txt");
    Path expectedTagManifestFile = dotbagitDir.resolve("tagmanifest-md5.txt");
    Path expectedBagitFile = dotbagitDir.resolve("bagit.txt");
    
    Bag bag = BagCreator.createDotBagit(folder, Arrays.asList(StandardSupportedAlgorithms.MD5), false);
    Assertions.assertEquals(new Version(2, 0), bag.getVersion());
    
    Assertions.assertTrue(Files.exists(expectedBagitFile));
    Assertions.assertTrue(Files.size(expectedBagitFile) > 0);
    
    Assertions.assertTrue(Files.exists(expectedManifestFile));
    Assertions.assertTrue(Files.size(expectedManifestFile) > 0);
    
    Assertions.assertTrue(Files.exists(expectedTagManifestFile));
    Assertions.assertTrue(Files.size(expectedTagManifestFile) > 0);
  }
  
  private class TestStructure{
    List<Path> regularPayloadFiles = new ArrayList<>();
    List<Path> hiddenPayloadFiles = new ArrayList<>();
  }
}
