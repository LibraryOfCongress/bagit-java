package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Map;

import gov.loc.repository.bagit.domain.Manifest;

/**
 * Creates the tag manifests by walking the tag files and calculating their checksums
 * Mainly used in {@link BagCreator}
 */
public class CreateTagManifestsVistor extends AbstractCreateManifestsVistor{
  
  public CreateTagManifestsVistor(final Map<Manifest, MessageDigest> manifestToMessageDigestMap, final boolean includeHiddenFiles){
    super(manifestToMessageDigestMap, includeHiddenFiles);
  }
  
  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    return abstractPreVisitDirectory(dir, "data");
  }
}
