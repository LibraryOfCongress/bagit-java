package gov.loc.repository.bagit;

import java.io.InputStream;
import java.util.Map;

public interface Manifest extends Map<String,String>, BagFile {
	
	enum Algorithm {
		MD5 ("md5", "MD5"), SHA1 ("sha1", "SHA-1"), SHA256 ("sha256", "SHA-256"), SHA512 ("sha512", "SHA-512");
		
		public String bagItAlgorithm;
		public String javaSecurityAlgorithm;
		
		Algorithm(String bagItAlgorithm, String javaSecurityAlgorithm) {
			this.bagItAlgorithm = bagItAlgorithm;
			this.javaSecurityAlgorithm = javaSecurityAlgorithm;
		}
		
		public static Algorithm valueOfBagItAlgorithm(String bagItAlgorithm) throws IllegalArgumentException {
			for(Algorithm algorithm : Algorithm.values()) {
				if (bagItAlgorithm.equals(algorithm.bagItAlgorithm)) {
					return algorithm;
				}
			}
			throw new IllegalArgumentException();
		}
		
		public static Algorithm valueOfJavaSecurityAlgorithm(String javaSecurityAlgorithm) throws IllegalArgumentException {
			for(Algorithm algorithm : Algorithm.values()) {
				if (javaSecurityAlgorithm.equals(algorithm.javaSecurityAlgorithm)) {
					return algorithm;
				}
			}
			throw new IllegalArgumentException();
		}
		
	}

	/**
	 * Returns true if this is a payload manifest, false otherwise.
	 * @return True if this is a payload manifest, false otherwise.
	 */
	public boolean isPayloadManifest();
	
	/**
	 * Returns true if this is a tag manifest, false otherwise.
	 * @return True if this is a tag manifest, false otherwise.
	 */
	public boolean isTagManifest();
	
	/**
	 * Returns the checksum algorithm.
	 * @return A checksum algorithm.
	 */
	public Algorithm getAlgorithm();
	
	/**
	 * Gets the original InputStream of a manifest file.
	 * @return The original InputStream.
	 */
	public InputStream originalInputStream();
	
	/**
	 * Returns a non-default manifest separator in String format.
	 * @return The non-default manifest separator in String format.
	 */
	public String getNonDefaultManifestSeparator();
	
	/**
	 * Sets the non-default manifest separator String.
	 * @param manifestSeparator Separates the contents of a manifest file.
	 */
	public void setNonDefaultManifestSeparator(String manifestSeparator);
	
}