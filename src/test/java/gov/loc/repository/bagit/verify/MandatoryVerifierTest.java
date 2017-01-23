package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.reader.BagReader;

public class MandatoryVerifierTest extends PrivateConstructorTest {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  private BagReader reader = new BagReader();

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MandatoryVerifier.class);
  }
  
  @Test(expected=FileNotInPayloadDirectoryException.class)
  public void testErrorWhenFetchItemsDontExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bad-fetch-bag").toURI());
    Bag bag = reader.read(rootDir);
    
    MandatoryVerifier.checkFetchItemsExist(bag.getItemsToFetch(), bag.getRootDir());
  }
  
  @Test(expected=MissingPayloadDirectoryException.class)
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(Paths.get(folder.getRoot().toURI()));
    File dataDir = new File(folder.getRoot(), "data");
    deleteDirectory(Paths.get(dataDir.toURI()));
    
    MandatoryVerifier.checkPayloadDirectoryExists(bag);
  }
  
  @Test(expected=MissingPayloadManifestException.class)
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(Paths.get(folder.getRoot().toURI()));
    File manifestFile = new File(folder.getRoot(), "manifest-md5.txt");
    manifestFile.delete();
    
    MandatoryVerifier.checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion());
  }
  
  @Test(expected=MissingBagitFileException.class)
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(Paths.get(folder.getRoot().toURI()));
    File bagitFile = new File(folder.getRoot(), "bagit.txt");
    bagitFile.delete();
    
    MandatoryVerifier.checkBagitFileExists(bag.getRootDir(), bag.getVersion());
  }
  
  private void copyBagToTestFolder() throws Exception{
    Files.walk(rootDir).forEach(path ->{
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
