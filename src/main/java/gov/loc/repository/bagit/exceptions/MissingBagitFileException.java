package gov.loc.repository.bagit.exceptions;

/**
 * The bagit.txt file is a required file. This class represents the error if that file is not present.
 */
public class MissingBagitFileException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingBagitFileException(String message){
    super(message);
  }
}
