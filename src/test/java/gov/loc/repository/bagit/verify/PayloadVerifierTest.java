package gov.loc.repository.bagit.verify;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.FileNotInManifestException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.reader.BagReader;

public class PayloadVerifierTest {
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  private BagReader reader = new BagReader();
  
  private PayloadVerifier sut;
  
  @BeforeEach
  public void setup(){
    sut = new PayloadVerifier(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  @Test
  public void testOtherConstructors() throws Exception {
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut = new PayloadVerifier();
    sut.verifyPayload(bag, true);
    
    sut = new PayloadVerifier(Executors.newCachedThreadPool());
    sut.verifyPayload(bag, true);
  }

  @Test
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInManifestDontExist").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInPayloadDirectoryException.class, 
        () -> { sut.verifyPayload(bag, true); });
  }
  
  @Test
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInPayloadDirAreNotInManifest").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.verifyPayload(bag, true); });
  }
  
  @Test
  public void testBagWithTagFilesInPayloadIsValid() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.verifyPayload(bag, true);
  }
  
  @Test
  public void testNotALlFilesListedInAllManifestsThrowsException() throws Exception{
    Path bagDir = Paths.get(new File("src/test/resources/notAllFilesListedInAllManifestsBag").toURI());
    Bag bag = reader.read(bagDir);
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.verifyPayload(bag, true); });
  }
}
