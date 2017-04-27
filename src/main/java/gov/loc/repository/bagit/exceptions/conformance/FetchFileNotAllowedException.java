package gov.loc.repository.bagit.exceptions.conformance;

import java.nio.file.Path;

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when a fetch file is found in a bag but is not allowed according to the bagit profile
 */
public class FetchFileNotAllowedException extends Exception {
private static final long serialVersionUID = 1L;
  
  public FetchFileNotAllowedException(final String message, final Path rootDir) {
    super(MessageFormatter.format(message, rootDir).getMessage());
  }
}
