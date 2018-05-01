package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.Assertions;

public class FileExistsVistor extends SimpleFileVisitor<Path>{
  private transient final Path originalBag;
  private transient final Path newBag;
  
  public FileExistsVistor(final Path originalBag, final Path newBag){
    this.originalBag = originalBag;
    this.newBag = newBag;
  }

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    Path relative = originalBag.relativize(dir);
    Assertions.assertTrue(Files.exists(newBag.resolve(relative)));
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException{
    final Path relative = originalBag.relativize(path);
    Assertions.assertTrue(Files.exists(newBag.resolve(relative)));
    
    return FileVisitResult.CONTINUE;
  }
  
}
