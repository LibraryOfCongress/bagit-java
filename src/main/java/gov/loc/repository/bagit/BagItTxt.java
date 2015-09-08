package gov.loc.repository.bagit;

/**
 * <p>A bag declaration tag file that identifies the container
 * as a {@link Bag} and gives the version of the BagIt specification
 * it adheres to, and the character encoding used for tag files.</p>
 *
 * <p>The bag declaration MUST be encoded in UTF-8, and MUST NOT
 * contain a byte-order mark (BOM).</p>
 */
public interface BagItTxt extends BagFile {
	
	static final String TYPE = "BagItTxt";
	
	/**
	 * Gets the version of the BagIt specification to which the
	 * bag conforms in the form of M.N, where M.N identifies
	 * the BagIt major (M) and minor (N) version numbers.
	 * @return The version of the bag.  Will never be null.
	 */
	String getVersion();
	
	/**
	 * Gets the character set encoding of tag files.
	 * @return The character set encoding of tag files.
	 */
	String getCharacterEncoding();
	
}