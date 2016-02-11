package gov.loc.repository.bagit.exceptions;

public class MissingPayloadDirectoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingPayloadDirectoryException(String message){
    super(message);
  }
}
