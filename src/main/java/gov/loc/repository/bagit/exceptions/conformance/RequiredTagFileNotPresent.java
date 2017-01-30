package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific manifest is not found
 */
public class RequiredTagFileNotPresent extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredTagFileNotPresent(final String message) {
    super(message);
  }
}
