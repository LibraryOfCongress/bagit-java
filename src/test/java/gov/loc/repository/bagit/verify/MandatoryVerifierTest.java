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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.reader.BagReader;

public class MandatoryVerifierTest extends PrivateConstructorTest {
  
  private Path rootDir = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
  private BagReader reader = new BagReader();

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MandatoryVerifier.class);
  }
  
  @Test
  public void testErrorWhenFetchItemsDontExist() throws Exception{
    rootDir = Paths.get(new File("src/test/resources/bad-fetch-bag").toURI());
    Bag bag = reader.read(rootDir);
    
    Assertions.assertThrows(FileNotInPayloadDirectoryException.class, 
        () -> { MandatoryVerifier.checkFetchItemsExist(bag.getItemsToFetch(), bag.getRootDir()); });
  }
  
  @Test
  public void testErrorWhenMissingPayloadDirectory() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(folder);
    Path dataDir = createDirectory("data");
    deleteDirectory(dataDir);
    
    Assertions.assertThrows(MissingPayloadDirectoryException.class, 
        () -> { MandatoryVerifier.checkPayloadDirectoryExists(bag); });
  }
  
  @Test
  public void testErrorWhenMissingPayloadManifest() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(folder);
    Path manifestFile = folder.resolve("manifest-md5.txt");
    Files.delete(manifestFile);
    
    Assertions.assertThrows(MissingPayloadManifestException.class, 
        () -> { MandatoryVerifier.checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion()); });
  }
  
  @Test
  public void testErrorWhenMissingBagitTextFile() throws Exception{
    copyBagToTestFolder();
    Bag bag = reader.read(folder);
    Path bagitFile = folder.resolve("bagit.txt");
    Files.delete(bagitFile);
    
    Assertions.assertThrows(MissingBagitFileException.class, 
        () -> { MandatoryVerifier.checkBagitFileExists(bag.getRootDir(), bag.getVersion()); });
  }
  
  private void copyBagToTestFolder() throws Exception{
    Files.walk(rootDir).forEach(path ->{
      try {
          Files.copy(path, Paths.get(path.toString().replace(
              rootDir.toString(),
              folder.toString())));
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
