package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when the bag's version is not in the acceptable list of versions
 */
public class BagitVersionIsNotAcceptableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public BagitVersionIsNotAcceptableException(final String message) {
    super(message);
  }
}
