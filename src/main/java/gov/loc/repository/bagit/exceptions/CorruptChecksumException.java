package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the calculated checksum is different than the manifest specified checksum
 */
public class CorruptChecksumException extends Exception {
  private static final long serialVersionUID = 1L;

  public CorruptChecksumException(String message){
    super(message);
  }
}
