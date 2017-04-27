package gov.loc.repository.bagit.verify;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import org.slf4j.helpers.MessageFormatter;

import gov.loc.repository.bagit.exceptions.FileNotInManifestException;

/**
 * Implements {@link SimpleFileVisitor} to ensure that the encountered file is in one of the manifests.
 */
public class PayloadFileExistsInAtLeastOneManifestVistor extends AbstractPayloadFileExistsInManifestsVistor {
  private transient final Set<Path> filesListedInManifests;

  public PayloadFileExistsInAtLeastOneManifestVistor(final Set<Path> filesListedInManifests, final boolean ignoreHiddenFiles) {
    super(ignoreHiddenFiles);
    this.filesListedInManifests = filesListedInManifests;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws FileNotInManifestException{
    if(Files.isRegularFile(path) && !filesListedInManifests.contains(path.normalize())){
      final String formattedMessage = messages.getString("file_not_in_any_manifest_error");
      throw new FileNotInManifestException(MessageFormatter.format(formattedMessage, path).getMessage());
    }
    logger.debug("[{}] is in at least one manifest", path);
    return FileVisitResult.CONTINUE;
  }
}
