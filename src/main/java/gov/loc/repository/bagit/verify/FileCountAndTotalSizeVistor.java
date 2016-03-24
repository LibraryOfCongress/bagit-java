package gov.loc.repository.bagit.verify;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link SimpleFileVisitor} to ensure that the encountered file is in one of the manifests.
 */
public class FileCountAndTotalSizeVistor extends SimpleFileVisitor<Path> {
  private static final Logger logger = LoggerFactory.getLogger(FileCountAndTotalSizeVistor.class);
  
  private final boolean ignoreHiddenFiles;
  private long totalSize = 0;
  private long count = 0;

  public FileCountAndTotalSizeVistor(boolean ignoreHiddenFiles) {
    this.ignoreHiddenFiles = ignoreHiddenFiles;
  }
  
  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if(ignoreHiddenFiles && Files.isHidden(dir)){
      logger.debug("Skipping {} cause ignore hidden files/directories", dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException{
    if(!ignoreHiddenFiles && Files.isHidden(path) && !path.endsWith(".keep")){
      logger.debug("Skipping [{}] since we are ignoring hidden files", path);
    }
    else{
      count++;
      long size = Files.size(path);
      logger.debug("File [{}] hash a size of [{}] bytes", path, size);
      totalSize += size;
    }
    
    return FileVisitResult.CONTINUE;
  }

  public long getCount() {
    return count;
  }

  public long getTotalSize() {
    return totalSize;
  }
}
