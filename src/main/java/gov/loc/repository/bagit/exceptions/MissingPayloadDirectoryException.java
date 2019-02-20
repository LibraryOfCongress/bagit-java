package gov.loc.repository.bagit.exceptions;

import java.nio.file.Path;

import org.slf4j.helpers.MessageFormatter;

/**
 * The payload directory is a required file. This class represents the error if it is not found.
 */
public class MissingPayloadDirectoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingPayloadDirectoryException(final String message, final Path path){
    super(MessageFormatter.format(message, path).getMessage());
  }
}
