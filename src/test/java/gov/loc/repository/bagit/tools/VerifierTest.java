package gov.loc.repository.bagit.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.VerifyResponse;
import gov.loc.repository.bagit.reader.BagReader;

public class VerifierTest extends Assert{
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
  
  @Test
  public void testIsValid() throws Exception{
    Bag bag = BagReader.read(rootDir);
    
    VerifyResponse response = Verifier.isValid(bag);
    
    assertFalse(response.hasError());
    assertEquals(0, response.getErrorMessages().size());
  }
  
  @Test
  public void testIsComplete() throws Exception{
    Bag bag = BagReader.read(rootDir);
    
    VerifyResponse response = Verifier.isComplete(bag);
    
    assertFalse(response.hasError());
    assertEquals(0, response.getErrorMessages().size());
  }
  
  @Test
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("filesInManifestDontExist").getFile());
    File missingFile = new File(rootDir, "data/test1.txt");
    Bag bag = BagReader.read(rootDir);
    
    VerifyResponse response = Verifier.isComplete(bag);
    
    assertTrue(response.hasError());
    assertTrue(response.getErrorMessages().contains("Bag lists file [" + missingFile + "] in manifest but it does not exist"));
  }
  
  @Test
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("filesInPayloadDirAreNotInManifest").getFile());
    File extraFile = new File(rootDir, "data/test1.txt");
    Bag bag = BagReader.read(rootDir);
    
    VerifyResponse response = Verifier.isComplete(bag);
    
    assertTrue(response.hasError());
    assertTrue(response.getErrorMessages().contains("File " + Paths.get(extraFile.toURI()) + " is in the payload directory but isn't listed in any of the manifests!"));
  }
  
  @Test
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(folder.getRoot());
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    bagitFile.delete();
    
    VerifyResponse response = Verifier.isValid(bag);
    
    assertTrue(response.hasError());
    assertTrue(response.getErrorMessages().contains("File [" + bagitFile + "] should exist but it doesn't"));
  }
  
  @Test
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(folder.getRoot());
    File dataDir = new File(folder.getRoot(), "data");
    deleteDirectory(Paths.get(dataDir.toURI()));
    
    VerifyResponse response = Verifier.isValid(bag);
    
    assertTrue(response.hasError());
    assertTrue(response.getErrorMessages().contains("File [" + dataDir + "] should exist but it doesn't"));
  }
  
  @Test
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(folder.getRoot());
    File manifestFile = new File(folder.getRoot(), "manifest-md5.txt");
    manifestFile.delete();
    
    VerifyResponse response = Verifier.isValid(bag);
    assertTrue(response.hasError());
    assertTrue(response.getErrorMessages().contains("Bag does not contain any payload manifest files!"));
  }
  
  private void copyBagToTestFolder() throws Exception{
    Files.walk(Paths.get(rootDir.toURI())).forEach(path ->{
      try {
          Files.copy(path, Paths.get(path.toString().replace(
              rootDir.toString(),
              folder.getRoot().toString())));
      } catch (Exception e) {}});
  }
  
  private void deleteDirectory(Path directory) throws Exception{
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    });
  }

}
