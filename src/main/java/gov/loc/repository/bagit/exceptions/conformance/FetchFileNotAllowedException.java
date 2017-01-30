package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a fetch file is found in a bag but is not allowed according to the bagit profile
 */
public class FetchFileNotAllowedException extends Exception {
private static final long serialVersionUID = 1L;
  
  public FetchFileNotAllowedException(final String message) {
    super(message);
  }
}
