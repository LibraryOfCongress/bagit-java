package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when a metadata's value is not in the acceptable list of values
 */
public class MetatdataValueIsNotAcceptable extends Exception {
private static final long serialVersionUID = 1L;
  
  public MetatdataValueIsNotAcceptable(final String message) {
    super(message);
  }
}
