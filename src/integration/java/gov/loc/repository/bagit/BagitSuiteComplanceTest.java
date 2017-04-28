package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
public class BagitSuiteComplanceTest extends Assert {
  private static final Logger logger = LoggerFactory.getLogger(BagitSuiteComplanceTest.class);
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private static final Path complianceRepoRootDir = Paths.get("bagit-conformance-suite");
  private static final BagTestCaseVistor visitor = new BagTestCaseVistor();
  private static final BagReader reader = new BagReader();
  private static final BagVerifier verifier = new BagVerifier();
  
  @BeforeClass
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
    
    assertEquals("every test case should throw an error", visitor.getInvalidTestCases().size(), errorCount);
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
    
    assertEquals("every test case should throw an error", osSpecificInvalidPaths.size(), errorCount);
    logger.debug("Count of all errors found in os specific invalid cases: {}", map);
  }
  
  @Test
  public void testWarnings() throws Exception{
    Set<BagitWarning> warnings;
    
    for(Path bagDir : visitor.getWarningTestCases()){
      warnings = BagLinter.lintBag(bagDir);
      assertTrue(warnings.size() > 0);
    }
  }
  
  @Test
  public void testReadWriteProducesSameBag() throws Exception{
    Bag bag;
    Path newBagDir;
    
    for(final Path bagDir : visitor.getValidTestCases()){
      newBagDir = folder.newFolder().toPath();
      bag = reader.read(bagDir);
      BagWriter.write(bag, newBagDir);
      
      testTagFileContents(bag, newBagDir);
      
      testBagsStructureAreEqual(bagDir, newBagDir);
    }
  }
  
  private void testTagFileContents(final Bag originalBag, final Path newBagDir) throws IOException{
    assertTrue("bagit.txt files differ", 
        compareFileContents(originalBag.getRootDir().resolve("bagit.txt"), 
            newBagDir.resolve("bagit.txt"), StandardCharsets.UTF_8));
    
    if(originalBag.getVersion().isSameOrOlder(new Version(0, 95))){
      assertTrue("package-info.txt files differ", 
          compareFileContents(originalBag.getRootDir().resolve("package-info.txt"), 
              newBagDir.resolve("package-info.txt"), originalBag.getFileEncoding()));
    }
    else{
      if(Files.exists(originalBag.getRootDir().resolve("bag-info.txt"))){
        assertTrue("bag-info.txt files differ", 
            compareFileContents(originalBag.getRootDir().resolve("bag-info.txt"), 
                newBagDir.resolve("bag-info.txt"), originalBag.getFileEncoding()));
      }
    }
    
  }
  
  //return true if the content is the same disregarding line endings
  private final boolean compareFileContents(final Path file1, final Path file2, final Charset encoding) throws IOException {
    List<String> lines1 = Files.readAllLines(file1, encoding);
    List<String> lines2 = Files.readAllLines(file2, encoding);
    
    for(int index=0; index<lines1.size(); index++){
      String strippedLine1 = lines1.get(index).replaceAll("\\r|\\n", "");
      String strippedLine2 = lines2.get(index).replaceAll("\\r|\\n", "");
      if(!strippedLine1.equals(strippedLine2)){
        return false;
      }
    }
    
    return true;
  }
  
  private void testBagsStructureAreEqual(Path originalBag, Path newBag) throws IOException{
    Files.walkFileTree(originalBag, new FileExistsVistor(originalBag, newBag));
  }
}
