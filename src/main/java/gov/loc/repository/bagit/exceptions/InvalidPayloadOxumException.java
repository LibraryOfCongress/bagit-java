package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the calculated total bytes or number of files for 
 * the payload-oxum is different than the supplied values
 */
public class InvalidPayloadOxumException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidPayloadOxumException(String message){
    super(message);
  }
}
