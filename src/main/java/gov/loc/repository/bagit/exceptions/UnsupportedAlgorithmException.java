package gov.loc.repository.bagit.exceptions;

/**
 * When there is no class for the named algorithm
 */
public class UnsupportedAlgorithmException extends Exception {
  private static final long serialVersionUID = 1L;

  public UnsupportedAlgorithmException(String message){
    super(message);
  }
}
