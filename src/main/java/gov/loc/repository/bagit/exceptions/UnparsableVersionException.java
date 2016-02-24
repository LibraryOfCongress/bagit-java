package gov.loc.repository.bagit.exceptions;

/**
 * A bagit bag needs at least one payload manifest. This class represents the error if at least one payload manifest isn't found.
 */
public class UnparsableVersionException extends Exception {
  private static final long serialVersionUID = 1L;

  public UnparsableVersionException(String message){
    super(message);
  }
}
