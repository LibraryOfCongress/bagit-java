package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the calculated checksum is different than the manifest specified checksum
 */
public class InvalidPayloadOxumException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidPayloadOxumException(String message){
    super(message);
  }
}
