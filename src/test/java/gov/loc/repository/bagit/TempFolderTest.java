package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

abstract public class TempFolderTest {
  protected Path folder;
  
  @BeforeEach
  public void setupTempFolder() throws IOException{
    folder = Files.createTempDirectory("junitTempFolder");
  }

  @AfterEach
  public void teardownTempFolder() throws IOException{
    delete(folder);
    Assertions.assertFalse(Files.exists(folder));
    //Assertions.assertEquals(0, Files.list(folder).count());
  }
  
  public Path createDirectory(String name) throws IOException {
    Path newDirectory = folder.resolve(name);
    return Files.createDirectories(newDirectory);
  }
  
  public Path createFile(String name) throws IOException {
    Path newFile = folder.resolve(name);
    return Files.createFile(newFile);
  }
  
  public Path copyBagToTempFolder(Path bagFolder) throws IOException{
	  Path bagCopyDir = createDirectory(bagFolder.getFileName() + "_copy");
	  Files.walkFileTree(bagFolder, new SimpleFileVisitor<Path>() {

	      @Override
	      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	    	Path relative = bagFolder.relativize(file);
	    	if(relative.getParent() != null) {
	    	  Files.createDirectories(bagCopyDir.resolve(relative.getParent()));
	    	}
	    	Files.copy(file, bagCopyDir.resolve(relative));
	        return FileVisitResult.CONTINUE;
	      }
	    });
	  
	  return bagCopyDir;
  }
  
  protected void delete(Path tempDirectory) throws IOException {
    Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return deleteAndContinue(file);
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return deleteAndContinue(dir);
      }

      private FileVisitResult deleteAndContinue(Path path) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
      }
    });
  }

}
