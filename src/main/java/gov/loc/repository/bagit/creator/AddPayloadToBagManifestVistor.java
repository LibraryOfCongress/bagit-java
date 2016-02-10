package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.Hasher;

public class AddPayloadToBagManifestVistor extends SimpleFileVisitor<Path>{
  private static final Logger logger = LoggerFactory.getLogger(AddPayloadToBagManifestVistor.class);
  
  private final Manifest manifest;
  private final MessageDigest messageDigest;
  private final boolean ignoreHiddenFiles;
  
  public AddPayloadToBagManifestVistor(Manifest manifest, MessageDigest messageDigest, boolean ignoreHiddenFiles){
    this.manifest = manifest;
    this.messageDigest = messageDigest;
    this.ignoreHiddenFiles = ignoreHiddenFiles;
  }
  
  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if(ignoreHiddenFiles && Files.isHidden(dir)){
      logger.debug("Skipping [{}] since we are ignoring hidden files", dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)throws IOException{
    if(Files.isRegularFile(path)){
      InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
      String hash = Hasher.hash(inputStream, messageDigest);
      manifest.getFileToChecksumMap().put(path.toFile(), hash);
    }
    
    return FileVisitResult.CONTINUE;
  }
}
