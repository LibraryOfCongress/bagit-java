package gov.loc.repository.bagit.verify;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.helpers.MessageFormatter;

import gov.loc.repository.bagit.exceptions.FileNotInManifestException;

/**
 * Implements {@link SimpleFileVisitor} to ensure that the encountered file is in one of the manifests.
 */
public class PayloadFileExistsInAtLeastOneManifestVistor extends AbstractPayloadFileExistsInManifestsVistor {
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private transient final Set<Path> filesListedInManifests;

  public PayloadFileExistsInAtLeastOneManifestVistor(final Set<Path> filesListedInManifests, final boolean ignoreHiddenFiles) {
    super(ignoreHiddenFiles);
    this.filesListedInManifests = filesListedInManifests;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException, FileNotInManifestException{
	if(Files.isHidden(path) && ignoreHiddenFiles){
	  logger.debug(messages.getString("skipping_hidden_file"), path);
  }
	else {
	  if(Files.isRegularFile(path) && !filesListedInManifests.contains(path.normalize())){
      final String formattedMessage = messages.getString("file_not_in_any_manifest_error");
      throw new FileNotInManifestException(MessageFormatter.format(formattedMessage, path).getMessage());
    }
    logger.debug(messages.getString("file_in_at_least_one_manifest"), path);
	}
	return FileVisitResult.CONTINUE;
  }

}
