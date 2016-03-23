package gov.loc.repository.bagit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;

public class FileExistsVistor extends SimpleFileVisitor<Path>{
  private final Path originalBag;
  private final Path newBag;
  
  public FileExistsVistor(Path originalBag, Path newBag){
    this.originalBag = originalBag;
    this.newBag = newBag;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    Path relative = originalBag.relativize(dir);
    Assert.assertTrue(Files.exists(newBag.resolve(relative)));
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)throws IOException{
    Path relative = originalBag.relativize(path);
    Assert.assertTrue(Files.exists(newBag.resolve(relative)));
    
    return FileVisitResult.CONTINUE;
  }
  
}
