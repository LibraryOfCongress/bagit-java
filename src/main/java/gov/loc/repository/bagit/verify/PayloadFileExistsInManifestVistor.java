package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.SimpleResponse;

public class PayloadFileExistsInManifestVistor extends SimpleFileVisitor<Path> {
  private static final Logger logger = LoggerFactory.getLogger(PayloadFileExistsInManifestVistor.class);
  private final Set<File> filesListedInManifests;
  private final SimpleResponse response;
  private final boolean ignoreHiddenFiles;

  public PayloadFileExistsInManifestVistor(Set<File> filesListedInManifests, SimpleResponse response, boolean ignoreHiddenFiles) {
    this.filesListedInManifests = filesListedInManifests;
    this.response = response;
    this.ignoreHiddenFiles = ignoreHiddenFiles;
  }
  
  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if(ignoreHiddenFiles && Files.isHidden(dir)){
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)throws IOException{
    if(Files.isRegularFile(path) && !filesListedInManifests.contains(path.toFile())){
      logger.error("File [{}] is in the payload directory but isn't listed in any of the manifests", path);
      response.setErrored(true);
      response.getErrorMessages().add("File " + path + " is in the payload directory but isn't listed in any of the manifests");
    }
    return FileVisitResult.CONTINUE;
  }
}
