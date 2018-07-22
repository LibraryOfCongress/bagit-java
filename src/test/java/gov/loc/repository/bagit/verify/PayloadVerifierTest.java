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
  
  private ManifestVerifier sut;
  
  @BeforeEach
  public void setup(){
    sut = new ManifestVerifier(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  @Test
  public void testOtherConstructors() throws Exception {
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut = new ManifestVerifier();
    sut.verifyManifests(bag, true);
    
    sut = new ManifestVerifier(Executors.newCachedThreadPool());
    sut.verifyManifests(bag, true);
  }

  @Test
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInManifestDontExist").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInPayloadDirectoryException.class, 
        () -> { sut.verifyManifests(bag, true); });
  }
  
  @Test
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/filesInPayloadDirAreNotInManifest").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.verifyManifests(bag, true); });
  }
  
  @Test
  public void testBagWithTagFilesInPayloadIsValid() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bags/v0_96/bag-with-tagfiles-in-payload-manifest").toURI());
    Bag bag = reader.read(rootDir);
    
    sut.verifyManifests(bag, true);
  }
  
  @Test
  public void testNotALlFilesListedInAllManifestsThrowsException() throws Exception{
    Path bagDir = Paths.get(new File("src/test/resources/notAllFilesListedInAllManifestsBag").toURI());
    Bag bag = reader.read(bagDir);
    Assertions.assertThrows(FileNotInManifestException.class, 
        () -> { sut.verifyManifests(bag, true); });
  }
}
