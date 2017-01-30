package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific manifest is not found (payload or tag)
 */
public class RequiredManifestNotPresent extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredManifestNotPresent(final String message) {
    super(message);
  }
}
