package gov.loc.repository.bagit.exceptions;

public class FileNotInPayloadDirectoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public FileNotInPayloadDirectoryException(String message){
    super(message);
  }
}
