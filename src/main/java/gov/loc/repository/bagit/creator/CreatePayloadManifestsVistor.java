package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Map;

import gov.loc.repository.bagit.domain.Manifest;

/**
 * Creates the payload manifests by walking the payload files and calculating their checksums
 * Mainly used in {@link BagCreator}
 */
public class CreatePayloadManifestsVistor extends AbstractCreateManifestsVistor{
  
  public CreatePayloadManifestsVistor(final Map<Manifest, MessageDigest> manifestToMessageDigestMap, final boolean includeHiddenFiles){
    super(manifestToMessageDigestMap, includeHiddenFiles);
  }
  
  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    return abstractPreVisitDirectory(dir, ".bagit");
  }
}
