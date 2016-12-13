package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the bag manifest file does not conform to the bagit spec, 
 * namely: <br>
 * &lt;CHECKSUM&gt; &lt;data/PATH/FILENAME&gt; 
 * <br>
 */
public class InvalidManifestFormatException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidManifestFormatException(final String message){
    super(message);
  }
}
