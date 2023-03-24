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

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.TempFolderTest;

public class CheckIfFileExistsTaskTest extends TempFolderTest {

  @Test
  public void testNormalizedFileExists() throws Exception{
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    CountDownLatch latch = new CountDownLatch(1);
    Set<Path> missingFiles = new ConcurrentSkipListSet<>();
    String filename = "Núñez.txt";
    String filenameNFC = Normalizer.normalize(filename, Normalizer.Form.NFC);
    String filenameNFD = Normalizer.normalize(filename, Normalizer.Form.NFD);
    Path NFCPath = folder.resolve(filenameNFC);
    
    createFile(filenameNFD);
    
    CheckIfFileExistsTask sut = new CheckIfFileExistsTask(NFCPath, missingFiles, latch);
    
    executor.execute(sut);
    latch.await();
    executor.shutdown();
    
    Assertions.assertTrue(missingFiles.size() == 0);
  }
}
