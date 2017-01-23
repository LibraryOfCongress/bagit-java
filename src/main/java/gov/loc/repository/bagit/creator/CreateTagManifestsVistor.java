package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import gov.loc.repository.bagit.hash.Hasher;

/**
 * Creates the tag manifests by walking the tag files and calculating their checksums
 * Mainly used in {@link BagCreator}
 */
public class CreateTagManifestsVistor extends AbstractCreateManifestsVistor{
  
  public CreateTagManifestsVistor(final Map<String, Hasher> bagitNameToHasherMap, final boolean includeHiddenFiles){
    super(bagitNameToHasherMap, includeHiddenFiles);
  }
  
  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    return abstractPreVisitDirectory(dir, "data");
  }
}
