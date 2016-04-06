package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the manifest has been crafted to point to a directory outside the bag.
 * Most likely to try and overwrite an important system file.
 */
public class MaliciousManifestException extends Exception {
  private static final long serialVersionUID = 1L;

  public MaliciousManifestException(String message){
    super(message);
  }
}
