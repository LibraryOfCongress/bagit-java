package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific metadata field is not found
 */
public class RequiredMetadataFieldNotPresent extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredMetadataFieldNotPresent(final String message) {
    super(message);
  }
}
