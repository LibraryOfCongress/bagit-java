package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when a specific bag file does not conform to its bagit specfication format
 */
public class InvalidBagitFileFormatException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidBagitFileFormatException(final String message){
    super(message);
  }
  
  public InvalidBagitFileFormatException(final String message, final Exception e){
    super(message, e);
  }
}
