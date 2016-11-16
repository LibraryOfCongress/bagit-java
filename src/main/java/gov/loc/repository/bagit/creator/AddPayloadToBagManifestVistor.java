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
import gov.loc.repository.bagit.util.PathUtils;

/**
 * An implementation of the {@link SimpleFileVisitor} class that optionally avoids hidden files.
 * Mainly used in {@link BagCreator}
 */
public class AddPayloadToBagManifestVistor extends SimpleFileVisitor<Path>{
  private static final Logger logger = LoggerFactory.getLogger(AddPayloadToBagManifestVistor.class);
  
  private transient final Manifest manifest;
  private transient final MessageDigest messageDigest;
  private transient final boolean includeHiddenFiles;
  
  public AddPayloadToBagManifestVistor(final Manifest manifest, final MessageDigest messageDigest, final boolean includeHiddenFiles){
    this.manifest = manifest;
    this.messageDigest = messageDigest;
    this.includeHiddenFiles = includeHiddenFiles;
  }
  
  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    if(!includeHiddenFiles && Files.isHidden(dir)){
      logger.debug("Skipping [{}] since we are ignoring hidden files", dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    //needed because Files.isHidden() doesn't work if the file is a directory on Windows
    if(!includeHiddenFiles && PathUtils.isHiddenWindowsFile(dir)){
      logger.debug("Skipping [{}] since we are ignoring hidden files", dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    //@Incubating
    if(dir.endsWith(".bagit")){ 
      logger.debug("Skipping .bagit directory cause it shouldn't be in the payload manifest");
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException{
    if(!includeHiddenFiles && Files.isHidden(path) && !path.endsWith(".keep")){
      logger.debug("Skipping [{}] since we are ignoring hidden files", path);
    }
    else{
      final InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
      final String hash = Hasher.hash(inputStream, messageDigest);
      logger.debug("Adding [{}] to manifest with hash [{}]", path, hash);
      manifest.getFileToChecksumMap().put(path, hash); 
    }
    
    return FileVisitResult.CONTINUE;
  }
}
