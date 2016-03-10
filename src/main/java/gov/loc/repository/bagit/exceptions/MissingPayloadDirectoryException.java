package gov.loc.repository.bagit.exceptions;

/**
 * The payload directory is a required file. This class represents the error if it is not found.
 */
public class MissingPayloadDirectoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingPayloadDirectoryException(String message){
    super(message);
  }
}
