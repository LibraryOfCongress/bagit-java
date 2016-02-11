package gov.loc.repository.bagit.exceptions;

public class CorruptChecksumException extends Exception {
  private static final long serialVersionUID = 1L;

  public CorruptChecksumException(String message){
    super(message);
  }
}
