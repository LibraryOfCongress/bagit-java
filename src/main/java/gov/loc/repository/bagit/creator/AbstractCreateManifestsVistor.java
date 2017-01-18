package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * An implementation of the {@link SimpleFileVisitor} class that optionally avoids hidden files.
 * Mainly used in {@link BagCreator}
 */
public abstract class AbstractCreateManifestsVistor extends SimpleFileVisitor<Path>{
  private static final Logger logger = LoggerFactory.getLogger(AbstractCreateManifestsVistor.class);
  
  protected final Map<Manifest, MessageDigest> manifestToMessageDigestMap;
  protected final boolean includeHiddenFiles;
  
  public AbstractCreateManifestsVistor(final Map<Manifest, MessageDigest> manifestToMessageDigestMap, final boolean includeHiddenFiles){
    this.manifestToMessageDigestMap = manifestToMessageDigestMap;
    this.includeHiddenFiles = includeHiddenFiles;
  }
  
  public FileVisitResult abstractPreVisitDirectory(final Path dir, final String directoryToIgnore) throws IOException {
    if(!includeHiddenFiles && PathUtils.isHidden(dir) && !dir.endsWith(Paths.get(".bagit"))){
      logger.debug("Skipping [{}] since we are ignoring hidden files", dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    if(dir.endsWith(directoryToIgnore)){ 
      logger.debug("Skipping {} directory cause it shouldn't be in the manifest", dir);
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
      Hasher.hash(path, manifestToMessageDigestMap);
    }
    
    return FileVisitResult.CONTINUE;
  }

  public Map<Manifest, MessageDigest> getManifestToMessageDigestMap() {
    return manifestToMessageDigestMap;
  }

  public boolean isIncludeHiddenFiles() {
    return includeHiddenFiles;
  }
}
