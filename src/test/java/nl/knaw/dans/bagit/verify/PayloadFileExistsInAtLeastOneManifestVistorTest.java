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

import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.TempFolderTest;
import nl.knaw.dans.bagit.exceptions.FileNotInManifestException;

public class PayloadFileExistsInAtLeastOneManifestVistorTest extends TempFolderTest {

  @Test
  public void testFileNotInManifestException() throws Exception{
    
    PayloadFileExistsInAtLeastOneManifestVistor sut = new PayloadFileExistsInAtLeastOneManifestVistor(new HashSet<>(), true);
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.visitFile(createFile("aNewFile"), null); });
  }
}
