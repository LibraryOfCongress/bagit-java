package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.verify.BagVerifier;
import gov.loc.repository.bagit.writer.BagWriter;

public class ReaderWriterVerifierIntegrationTest {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testReaderWriterVersion93() throws Exception{
    BagVerifier verifier = new BagVerifier();
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_93/bag").toURI());
    Path outputDir = Paths.get(folder.newFolder().toURI());
    
    BagWriter writer = new BagWriter();
    
    Bag bag = BagReader.read(rootDir);
    verifier.isValid(bag, true);
    
    writer.write(bag, outputDir);
    testBagsEqual(rootDir, outputDir);
    
    verifier.isValid(BagReader.read(outputDir), true);
  }
  
  @Test
  public void testReaderWriterVersion94() throws Exception{
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_94/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    Path outputDir = Paths.get(folder.newFolder().toURI());
    BagWriter writer = new BagWriter();
    
    writer.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    BagVerifier verifier = new BagVerifier();
    verifier.isValid(BagReader.read(outputDir), true);
  }
  
  @Test
  public void testReaderWriterVersion95() throws Exception{
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_95/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    Path outputDir = Paths.get(folder.newFolder().toURI());
    BagWriter writer = new BagWriter();
    
    writer.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    BagVerifier verifier = new BagVerifier();
    verifier.isValid(BagReader.read(outputDir), true);
  }
  
  @Test
  public void testReaderWriterVersion96() throws Exception{
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_96/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    Path outputDir = Paths.get(folder.newFolder().toURI());
    BagWriter writer = new BagWriter();
    
    writer.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    BagVerifier verifier = new BagVerifier();
    verifier.isValid(BagReader.read(outputDir), true);
  }

  @Test
  public void testReaderWriterVersion97() throws Exception{
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    Path outputDir = Paths.get(folder.newFolder().toURI());
    BagWriter writer = new BagWriter();
    
    writer.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    BagVerifier verifier = new BagVerifier();
    verifier.isValid(BagReader.read(outputDir), true);
  }
  
  @Test
  public void testReaderWriterVersion2_0() throws Exception{
    Path rootDir = Paths.get(this.getClass().getClassLoader().getResource("bags/v2_0/bag").toURI());
    Bag bag = BagReader.read(rootDir);
    Path outputDir = Paths.get(folder.newFolder().toURI());
    BagWriter writer = new BagWriter();
    
    writer.write(bag, outputDir);
    
    testBagsEqual(rootDir, outputDir);
    BagVerifier verifier = new BagVerifier();
    verifier.isValid(BagReader.read(outputDir), true);
  }
  
  private void testBagsEqual(Path originalBag, Path newBag) throws IOException{
    Files.walkFileTree(originalBag, new FileExistsVistor(originalBag, newBag));
  }
}
