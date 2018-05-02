package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Map;
import java.util.ResourceBundle;

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
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  protected transient final Map<Manifest, MessageDigest> manifestToMessageDigestMap;
  protected transient final boolean includeHiddenFiles;
  
  public AbstractCreateManifestsVistor(final Map<Manifest, MessageDigest> manifestToMessageDigestMap, final boolean includeHiddenFiles){
    this.manifestToMessageDigestMap = manifestToMessageDigestMap;
    this.includeHiddenFiles = includeHiddenFiles;
  }
  
  public FileVisitResult abstractPreVisitDirectory(final Path dir, final String directoryToIgnore) throws IOException {
    if(!includeHiddenFiles && PathUtils.isHidden(dir) && !dir.endsWith(Paths.get(".bagit"))){
      logger.debug(messages.getString("skipping_hidden_file"), dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    if(dir.endsWith(directoryToIgnore)){ 
      logger.debug(messages.getString("skipping_ignored_directory"), dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException{
    if(!includeHiddenFiles && PathUtils.isHidden(path) && !path.endsWith(".keep")){
      logger.debug(messages.getString("skipping_hidden_file"), path);
    }
    else{
      Hasher.hash(path, manifestToMessageDigestMap);
    }
    
    return FileVisitResult.CONTINUE;
  }
}
