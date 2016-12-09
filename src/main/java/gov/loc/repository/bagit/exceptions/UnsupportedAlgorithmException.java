package gov.loc.repository.bagit.exceptions;

import java.security.MessageDigest;

/**
 * When the bag uses an checksum algorithm that is not supported by {@link MessageDigest}.
 */
public class UnsupportedAlgorithmException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public UnsupportedAlgorithmException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
