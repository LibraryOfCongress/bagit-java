package gov.loc.repository.bagit.exceptions;

/**
 * When there is no class for the named algorithm
 */
public class UnsupportedAlgorithmException extends Exception {
  private static final long serialVersionUID = 1L;

  public UnsupportedAlgorithmException(final String message){
    super(message);
  }
  
  public UnsupportedAlgorithmException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
