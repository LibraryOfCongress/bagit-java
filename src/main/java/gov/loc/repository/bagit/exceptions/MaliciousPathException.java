package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the path in a manifest or fetch file has been crafted to point to a file or 
 * directory outside the bag. Most likely to try and overwrite an important system file.
 */
public class MaliciousPathException extends Exception {
  private static final long serialVersionUID = 1L;

  public MaliciousPathException(final String message){
    super(message);
  }
}
