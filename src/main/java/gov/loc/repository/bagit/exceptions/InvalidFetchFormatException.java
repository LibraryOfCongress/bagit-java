package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when the bag fetch file does not conform to the bagit spec, 
 * namely: <br>
 * &lt;URL&gt; &lt;SIZE IN BYTES or -&gt; &lt;data/PATH/FILENAME&gt; 
 * <br>
 */
public class InvalidFetchFormatException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidFetchFormatException(final String message){
    super(message);
  }
}
