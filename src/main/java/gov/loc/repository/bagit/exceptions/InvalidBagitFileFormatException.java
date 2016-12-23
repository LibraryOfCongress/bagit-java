package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the bag manifest file does not conform to the bagit specfication format
 */
public class InvalidBagitFileFormatException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidBagitFileFormatException(final String message){
    super(message);
  }
  
  public InvalidBagitFileFormatException(final String message, Exception e){
    super(message, e);
  }
}
