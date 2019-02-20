package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TestUtils {
  public static boolean isExecutingOnWindows(){
    return System.getProperty("os.name").contains("Windows");
  }
  
  /**
   * walk a directory and make sure that files/folders are hidden if they start with a . on windows.
   * 
   * @param startingDir the directory to start walking
   * @throws IOException if there is a problem setting the file/folder to be hidden
   */
  public static void makeFilesHiddenOnWindows(Path startingDir) throws IOException {
    if (isExecutingOnWindows()) {
      Files.walkFileTree(startingDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException{
          String name = dir.getFileName().toString();
          if(name.startsWith(".") && !(name.equals(".keep") || name.equals(".bagit"))){
            Files.setAttribute(dir, "dos:hidden", Boolean.TRUE);
          }
          return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
          String name = path.getFileName().toString();
          if(name.startsWith(".") && !(name.equals(".keep") || name.equals(".bagit"))){
            Files.setAttribute(path, "dos:hidden", Boolean.TRUE);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }
}
