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
package nl.knaw.dans.bagit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.knaw.dans.bagit.domain.Bag;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.reader.BagReader;
import nl.knaw.dans.bagit.verify.BagVerifier;
import nl.knaw.dans.bagit.writer.BagWriter;

public class ReaderWriterVerifierIntegrationTest extends TempFolderTest {
  
  @Test
  public void testReaderWriterVersion93() throws Exception{
    try(BagVerifier verifier = new BagVerifier()){
      BagReader reader = new BagReader();
      Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_93/bag").toURI());
      Path outputDir = folder.resolve("version93");
      
      Bag bag = reader.read(rootDir);
      verifier.isValid(bag, true);
      
      BagWriter.write(bag, outputDir);
      testBagsEqual(rootDir, outputDir);
      
      verifier.isValid(reader.read(outputDir), true);
    }
  }
  
  @Test
  public void testReaderWriterVersion94() throws Exception{
    BagReader reader = new BagReader();
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_94/bag").toURI());
    Bag bag = reader.read(rootDir);
    Path outputDir = folder.resolve("version94");
    
    BagWriter.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    try(BagVerifier verifier = new BagVerifier()){
      verifier.isValid(reader.read(outputDir), true);
    }
  }
  
  @Test
  public void testReaderWriterVersion95() throws Exception{
    BagReader reader = new BagReader();
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_95/bag").toURI());
    Bag bag = reader.read(rootDir);
    Path outputDir = folder.resolve("version95");
    
    BagWriter.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    try(BagVerifier verifier = new BagVerifier()){
      verifier.isValid(reader.read(outputDir), true);
    }
  }
  
  @Test
  public void testReaderWriterVersion96() throws Exception{
    BagReader reader = new BagReader();
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_96/bag").toURI());
    Bag bag = reader.read(rootDir);
    Path outputDir = folder.resolve("version96");
    
    BagWriter.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    try(BagVerifier verifier = new BagVerifier()){
      verifier.isValid(reader.read(outputDir), true);
    }
  }

  @Test
  public void testReaderWriterVersion97() throws Exception{
    BagReader reader = new BagReader();
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = reader.read(rootDir);
    Path outputDir = folder.resolve("version97");
    
    BagWriter.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    try(BagVerifier verifier = new BagVerifier()){
      verifier.isValid(reader.read(outputDir), true);
    }
  }
  
  @Test
  public void testReaderWriterVersion2_0() throws Exception{
    BagReader reader = new BagReader();
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v2_0/bag").toURI());
    Bag bag = reader.read(rootDir);
    Path outputDir = folder.resolve("version2");
    
    BagWriter.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    
    try(BagVerifier verifier = new BagVerifier()){
      verifier.isValid(reader.read(outputDir), true);
    }
  }
  
  private void testBagsEqual(Path originalBag, Path newBag) throws IOException{
    Files.walkFileTree(originalBag, new FileExistsVistor(originalBag, newBag));
  }
}
