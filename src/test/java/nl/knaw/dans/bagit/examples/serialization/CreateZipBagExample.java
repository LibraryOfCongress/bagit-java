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
package nl.knaw.dans.bagit.examples.serialization;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nl.knaw.dans.bagit.TempFolderTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Example(s) for creating a zipped bag.
 */
public class CreateZipBagExample extends TempFolderTest {
  
  private Path bagRoot;
  private Path zippedBagPath;
  private OutputStream outputStream;
  
  @BeforeEach
  public void setup() throws IOException{
    bagRoot = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
    zippedBagPath = createFile("bag.zip");
    outputStream = Files.newOutputStream(zippedBagPath, StandardOpenOption.CREATE);
  }

  /**
   * <b> THIS IS JUST AN EXAMPLE. DO NOT USE IN PRODUCTION!</b>
   */
  @Test
  public void createZipBagWithJavaStandardLibrary(){
    try(ZipOutputStream zip = new ZipOutputStream(outputStream)){
      ZipVistor visitor = new ZipVistor(bagRoot, zip);
      Files.walkFileTree(bagRoot, visitor);
      
      Assertions.assertTrue(Files.exists(zippedBagPath));
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  class ZipVistor extends SimpleFileVisitor<Path>{
    private Path rootDir;
    private ZipOutputStream zip;
    
    public ZipVistor(Path rootDir, ZipOutputStream zip){
      this.rootDir = rootDir;
      this.zip = zip;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
      try{
        Path relative = rootDir.relativize(file);
        ZipEntry zipEntry = new ZipEntry(relative.toString());
        zip.putNextEntry(zipEntry);
        Files.copy(file, zip);
        zip.closeEntry();
      }
      catch(Exception e){
        e.printStackTrace();
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
