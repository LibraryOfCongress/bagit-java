package gov.loc.repository.bagit.exceptions;

import java.security.MessageDigest;

import org.slf4j.helpers.MessageFormatter;

/**
 * When the bag uses an checksum algorithm that is not supported by {@link MessageDigest}.
 */
public class UnsupportedAlgorithmException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public UnsupportedAlgorithmException(final String message, final String bagitAlgorithmName, final Throwable cause) {
    super(MessageFormatter.format(message, bagitAlgorithmName).getMessage(), cause);
  }
}
