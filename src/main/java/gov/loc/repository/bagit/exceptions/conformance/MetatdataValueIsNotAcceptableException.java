package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a metadata's value is not in the acceptable list of values
 */
public class MetatdataValueIsNotAcceptableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public MetatdataValueIsNotAcceptableException(final String message) {
    super(message);
  }
}
