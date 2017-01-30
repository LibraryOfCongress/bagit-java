package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific manifest is not found (payload or tag)
 */
public class RequiredManifestNotPresentException extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredManifestNotPresentException(final String message) {
    super(message);
  }
}
