package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific manifest type is not found, such as md5, sha1, etc (payload or tag)
 */
public class RequiredManifestNotPresentException extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredManifestNotPresentException(final String message) {
    super(message);
  }
}
