package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the bag metadata file does not conform to the bagit spec, 
 * namely: <br>
 * &lt;KEY&gt;:&lt;VALUE&gt; 
 * <br>or
 * <pre>&lt;KEY&gt;:&lt;VALUE&gt;
 *    &lt;VALUE CONTINUED&gt;</pre>
 */
public class InvalidBagMetadataException extends InvalidBagitFileFormatException {
  private static final long serialVersionUID = 1L;

  public InvalidBagMetadataException(final String message){
    super(message);
  }
}
