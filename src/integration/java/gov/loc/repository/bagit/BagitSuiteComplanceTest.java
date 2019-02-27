package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.conformance.BagLinter;
import gov.loc.repository.bagit.conformance.BagitWarning;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.exceptions.VerificationException;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.verify.BagVerifier;
import gov.loc.repository.bagit.writer.BagWriter;

/**
 * This class assumes that the compliance test suite repo has been cloned and is available locally
 */
public class BagitSuiteComplanceTest extends TempFolderTest {
  private static final Logger logger = LoggerFactory.getLogger(BagitSuiteComplanceTest.class);
  
  private static final Path complianceRepoRootDir = Paths.get("bagit-conformance-suite");
  private static final BagTestCaseVistor visitor = new BagTestCaseVistor();
  private static final BagReader reader = new BagReader();
  private static final BagVerifier verifier = new BagVerifier();
  
  @BeforeAll
  public static void setupOnce() throws IOException{
    if(!Files.exists(complianceRepoRootDir)){
      throw new IOException("bagit-conformance-suite git repo was not found, did you clone it?");
    }
    Files.walkFileTree(complianceRepoRootDir, visitor);
  }
  
  @Test
  public void testValidBags() throws Exception{
    Bag bag;
    
    for(final Path bagDir : visitor.getValidTestCases()){
      bag = reader.read(bagDir);
      verifier.isValid(bag, true);
    }
  }
  
  @Test
  public void testInvalidBags(){
    int errorCount = 0;
    Bag bag;
    ConcurrentMap<Class<? extends Exception>, AtomicLong> map = new ConcurrentHashMap<>();
    
    for(Path invalidBagDir : visitor.getInvalidTestCases()){
      try{
        bag = reader.read(invalidBagDir);
        verifier.isValid(bag, true);
        System.err.println(bag.getRootDir() + " should have failed but didn't!");
      }catch(InvalidBagitFileFormatException | IOException | UnparsableVersionException | 
        MissingPayloadManifestException | MissingBagitFileException | MissingPayloadDirectoryException | 
        FileNotInPayloadDirectoryException | InterruptedException | MaliciousPathException | 
        CorruptChecksumException | VerificationException | UnsupportedAlgorithmException e){
        
        logger.info("Found invalid os specific bag with message: {}", e.getMessage());
        map.putIfAbsent(e.getClass(), new AtomicLong(0));
        map.get(e.getClass()).incrementAndGet();
        errorCount++;
      }
    }
    
    Assertions.assertEquals(visitor.getInvalidTestCases().size(), errorCount, "every test case should throw an error");
    logger.debug("Count of all errors found in generic invalid cases: {}", map);
  }
  
  @Test
  public void testInvalidOperatingSystemSpecificBags(){
    int errorCount = 0;
    Bag bag;
    List<Path> osSpecificInvalidPaths = visitor.getLinuxOnlyTestCases();
    ConcurrentMap<Class<? extends Exception>, AtomicLong> map = new ConcurrentHashMap<>();
    
    if(TestUtils.isExecutingOnWindows()){
      osSpecificInvalidPaths = visitor.getWindowsOnlyTestCases();
    }
    
    for(Path invalidBagDir : osSpecificInvalidPaths){
      try{
        bag = reader.read(invalidBagDir);
        verifier.isValid(bag, true);
      }catch(InvalidBagitFileFormatException | IOException | UnparsableVersionException | 
        MissingPayloadManifestException | MissingBagitFileException | MissingPayloadDirectoryException | 
        FileNotInPayloadDirectoryException | InterruptedException | MaliciousPathException | 
        CorruptChecksumException | VerificationException | UnsupportedAlgorithmException e){

        logger.info("Found invalid os specific bag with message: {}", e.getMessage());
        map.putIfAbsent(e.getClass(), new AtomicLong(0));
        map.get(e.getClass()).incrementAndGet();
        errorCount++;
      }
    }
    
    Assertions.assertEquals(osSpecificInvalidPaths.size(), errorCount, "every test case should throw an error");
    logger.debug("Count of all errors found in os specific invalid cases: {}", map);
  }
  
  @Test
  public void testWarnings() throws Exception{
    Set<BagitWarning> warnings;
    
    for(Path bagDir : visitor.getWarningTestCases()){
      warnings = BagLinter.lintBag(bagDir);
      Assertions.assertTrue(warnings.size() > 0);
    }
  }
  
  @Test
  public void testReadWriteProducesSameBag() throws Exception{
    Bag bag;
    Path newBagDir;
    
    for(final Path bagDir : visitor.getValidTestCases()){
      newBagDir = folder.resolve("readWriteProducesSameBag");
      bag = reader.read(bagDir);
      BagWriter.write(bag, newBagDir);
      
      testTagFileContents(bag, newBagDir);
      
      testBagsStructureAreEqual(bagDir, newBagDir);
      delete(newBagDir);
    }
  }
  
  private void testTagFileContents(final Bag originalBag, final Path newBagDir) throws IOException{
    Path original = originalBag.getRootDir().resolve("bagit.txt");
    Path newFile = newBagDir.resolve("bagit.txt");
    Assertions.assertTrue(compareFileContents(original, newFile, StandardCharsets.UTF_8), "bagit.txt files differ");
    
    if(originalBag.getVersion().isSameOrOlder(new Version(0, 95))){
      original = originalBag.getRootDir().resolve("package-info.txt");
      newFile = newBagDir.resolve("package-info.txt");
      Assertions.assertTrue(compareFileContents(original, newFile, originalBag.getFileEncoding()), 
          original + " differs from " + newFile);
    }
    else{
      if(Files.exists(originalBag.getRootDir().resolve("bag-info.txt"))){
        original = originalBag.getRootDir().resolve("bag-info.txt");
        newFile = newBagDir.resolve("bag-info.txt");
        Assertions.assertTrue(compareFileContents(original,newFile, originalBag.getFileEncoding()),
            original + " differs from " + newFile);
      }
    }
    
  }
  
  //return true if the content is the same disregarding line endings
  private final boolean compareFileContents(final Path file1, final Path file2, final Charset encoding) throws IOException {
    List<String> lines1 = Files.readAllLines(file1, encoding);
    List<String> lines2 = Files.readAllLines(file2, encoding);
    
    List<String> strippedLines1 = new ArrayList<>(lines1.size());
    List<String> strippedLines2 = new ArrayList<>(lines2.size());
    
    for(int index=0; index<lines1.size(); index++){
      strippedLines1.add(lines1.get(index).replaceAll("\\r|\\n| ", ""));
      strippedLines2.add(lines2.get(index).replaceAll("\\r|\\n| ", ""));
    }
    
    return strippedLines1.containsAll(strippedLines2);
  }
  
  private void testBagsStructureAreEqual(Path originalBag, Path newBag) throws IOException{
    Files.walkFileTree(originalBag, new FileExistsVistor(originalBag, newBag));
  }
}
