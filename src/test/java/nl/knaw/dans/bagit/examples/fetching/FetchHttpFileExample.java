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
package nl.knaw.dans.bagit.examples.fetching;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import nl.knaw.dans.bagit.TempFolderTest;
import nl.knaw.dans.bagit.domain.FetchItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FetchHttpFileExample extends TempFolderTest {

  /**
   * <b> THIS IS JUST AN EXAMPLE. DO NOT USE IN PRODUCTION!</b>
   * 
   * @throws IOException if there is a problem getting the file
   */
  @Test
  public void fetchFileUsingJavaStandardLibrary() throws IOException{
    //in actual usage you would iterate over the list of FetchItem in the Bag
    FetchItem item = new FetchItem(new URL("https://en.wikipedia.org/wiki/Main_Page"), 0l, createFile("Main_page.html"));
    try{
      Files.copy(item.url.openStream(), item.path, StandardCopyOption.REPLACE_EXISTING);
      Assertions.assertTrue(Files.exists(item.path));
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}
