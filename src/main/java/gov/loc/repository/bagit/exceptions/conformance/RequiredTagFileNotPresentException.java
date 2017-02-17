package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific tag file is not found
 */
public class RequiredTagFileNotPresentException extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredTagFileNotPresentException(final String message) {
    super(message);
  }
}
