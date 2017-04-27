package gov.loc.repository.bagit.exceptions;

import java.nio.file.Path;

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent an error when the calculated checksum is different than the manifest specified checksum.
 */
public class CorruptChecksumException extends Exception {
  private static final long serialVersionUID = 1L;

  public CorruptChecksumException(final String message, final Path path, final String algorithm, final String hash, final String computedHash){
    super(MessageFormatter.arrayFormat(message, new Object[]{path, algorithm, hash, computedHash}).getMessage());
  }
}
