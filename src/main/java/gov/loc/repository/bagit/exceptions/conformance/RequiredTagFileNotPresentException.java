package gov.loc.repository.bagit.exceptions.conformance;

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when a specific tag file is not found
 */
public class RequiredTagFileNotPresentException extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredTagFileNotPresentException(final String message, final String requiredTagFilePath) {
    super(MessageFormatter.format(message, requiredTagFilePath).getMessage());
  }
}
