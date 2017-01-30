package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a specific metadata field is not found
 */
public class RequiredMetadataFieldNotPresentException extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredMetadataFieldNotPresentException(final String message) {
    super(message);
  }
}
