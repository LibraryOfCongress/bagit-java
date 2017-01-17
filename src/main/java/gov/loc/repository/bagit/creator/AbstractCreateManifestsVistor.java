package gov.loc.repository.bagit.creator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * An implementation of the {@link SimpleFileVisitor} class that optionally avoids hidden files.
 * Mainly used in {@link BagCreator}
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public abstract class AbstractCreateManifestsVistor extends SimpleFileVisitor<Path>{
  private static final Logger logger = LoggerFactory.getLogger(AbstractCreateManifestsVistor.class);
  private static final int _64_KB = 1024 * 64;
  private static final int CHUNK_SIZE = _64_KB;
  
  protected final Map<String, Manifest> bagitNameToManifestMap;
  protected final Map<String, Hasher> bagitNameToHasherMap;
  protected final boolean includeHiddenFiles;
  
  public AbstractCreateManifestsVistor(final Map<String, Hasher> bagitNameToHasherMap, final boolean includeHiddenFiles){
    this.bagitNameToManifestMap = new HashMap<>();
    for(final String bagitName : bagitNameToHasherMap.keySet()){
      bagitNameToManifestMap.put(bagitName, new Manifest(bagitName));
    }
    this.bagitNameToHasherMap = bagitNameToHasherMap;
    this.includeHiddenFiles = includeHiddenFiles;
  }
  
  public FileVisitResult abstractPreVisitDirectory(final Path dir, final String directoryToIgnore) throws IOException {
    if(!includeHiddenFiles && PathUtils.isHidden(dir)){
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
      try(final InputStream inputStream = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))){
        final byte[] buffer = new byte[CHUNK_SIZE];
        int numberOfBytesRead = inputStream.read(buffer);
        
        while(numberOfBytesRead != -1) {
          for(final Hasher hasher : bagitNameToHasherMap.values()){
            hasher.update(buffer, numberOfBytesRead);
          }
          numberOfBytesRead = inputStream.read(buffer);
        }
        
        for(final Entry<String, Hasher> entry: bagitNameToHasherMap.entrySet()){
          bagitNameToManifestMap.get(entry.getKey()).getFileToChecksumMap().put(path, entry.getValue().getCalculatedValue());
          entry.getValue().clear(); //reset the hasher's state since we are done calculating
        }
      }
    }
    
    return FileVisitResult.CONTINUE;
  }
  
  public Set<Manifest> getManifests(){
    return new HashSet<>(bagitNameToManifestMap.values());
  }

  public Map<String, Manifest> getBagitNameToManifestMap() {
    return bagitNameToManifestMap;
  }

  public Map<String, Hasher> getBagitNameToHasherMap() {
    return bagitNameToHasherMap;
  }

  public boolean isIncludeHiddenFiles() {
    return includeHiddenFiles;
  }
}
