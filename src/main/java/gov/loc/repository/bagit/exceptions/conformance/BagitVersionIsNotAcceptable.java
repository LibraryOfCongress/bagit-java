package gov.loc.repository.bagit.exceptions.conformance;

/**
 * Class to represent when the bag's version is not in the acceptable list of versions
 */
public class BagitVersionIsNotAcceptable extends Exception {
private static final long serialVersionUID = 1L;
  
  public BagitVersionIsNotAcceptable(final String message) {
    super(message);
  }
}
