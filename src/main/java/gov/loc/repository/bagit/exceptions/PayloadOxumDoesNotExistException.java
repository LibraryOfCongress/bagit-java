package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the calculated checksum is different than the manifest specified checksum
 */
public class PayloadOxumDoesNotExistException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PayloadOxumDoesNotExistException(String message){
    super(message);
  }
}
