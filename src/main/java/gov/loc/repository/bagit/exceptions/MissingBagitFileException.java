package gov.loc.repository.bagit.exceptions;

public class MissingBagitFileException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingBagitFileException(String message){
    super(message);
  }
}
