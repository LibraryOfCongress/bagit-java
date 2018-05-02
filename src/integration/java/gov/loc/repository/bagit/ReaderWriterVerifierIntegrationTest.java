package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.verify.BagVerifier;
import gov.loc.repository.bagit.writer.BagWriter;

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
