package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.domain.SupportedAlgorithm;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInManifestException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.reader.BagReader;

public class VerifierTest extends Assert{
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private File rootDir = new File(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
  private SupportedAlgorithm algorithm = StandardSupportedAlgorithms.valueOf("MD5");
  
  @Test
  public void testStandardSupportedAlgorithms() throws Exception{
    List<String> algorithms = Arrays.asList("md5", "sha1", "sha256", "sha512");
    for(String algorithm : algorithms){
      Manifest manifest = new Manifest(algorithm);
      Verifier.checkHashes(manifest, StandardSupportedAlgorithms.valueOf(algorithm.toUpperCase()));
    }
  }
  
  @Test
  public void testBagWithTagFilesInPayloadIsValid() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/bag-with-tagfiles-in-payload-manifest").getFile());
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isValid(bag, algorithm, true);
  }
  
  @Test
  public void testIsValid() throws Exception{
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isValid(bag, algorithm, true);
  }
  
  @Test
  public void testIsComplete() throws Exception{
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenFetchItemsDontExist() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("bags/v0_96/holey-bag").getFile());
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenManifestListFileThatDoesntExist() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("filesInManifestDontExist").getFile());
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isComplete(bag, true);
  }
  
  @Test(expected=FileNotInManifestException.class)
  public void testErrorWhenFileIsntInManifest() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("filesInPayloadDirAreNotInManifest").getFile());
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isComplete(bag, true);
  }
  
  @Test(expected=CorruptChecksumException.class)
  public void testCorruptPayloadFile() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("corruptPayloadFile").getFile());
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isValid(bag, algorithm, true);
  }
  
  @Test(expected=CorruptChecksumException.class)
  public void testCorruptTagFile() throws Exception{
    rootDir = new File(getClass().getClassLoader().getResource("corruptTagFile").getFile());
    Bag bag = BagReader.read(rootDir);
    
    Verifier.isValid(bag, algorithm, true);
  }
  
  @Test(expected=MissingBagitFileException.class)
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(folder.getRoot());
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    bagitFile.delete();
    
    Verifier.isValid(bag, algorithm, true);
  }
  
  @Test(expected=MissingPayloadDirectoryException.class)
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(folder.getRoot());
    File dataDir = new File(folder.getRoot(), "data");
    deleteDirectory(Paths.get(dataDir.toURI()));
    
    Verifier.isValid(bag, algorithm, true);
  }
  
  @Test(expected=MissingPayloadManifestException.class)
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = BagReader.read(folder.getRoot());
    File manifestFile = new File(folder.getRoot(), "manifest-md5.txt");
    manifestFile.delete();
    
    Verifier.isValid(bag, algorithm, true);
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
