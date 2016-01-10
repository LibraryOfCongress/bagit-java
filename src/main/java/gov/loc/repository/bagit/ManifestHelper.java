package gov.loc.repository.bagit;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.Bag.BagConstants;

public class ManifestHelper {
	
	private static final Log log = LogFactory.getLog(ManifestHelper.class);
	
	/**
	 * Returns true if the filename matches the BagConstants payload manifest
	 * prefix and suffix, false otherwise.
	 * @param filename Name of the manifest file.
	 * @param bagConstants Contains names for constants associated with a bag.
	 * @return True if the filename matches the BagConstants payload manifest
	 * prefix and suffix, false otherwise.
	 */
	public static boolean isPayloadManifest(String filename, BagConstants bagConstants) {
		if (filename.startsWith(bagConstants.getPayloadManifestPrefix()) && filename.endsWith(bagConstants.getPayloadManifestSuffix())) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the filename matches the BagConstants tag manifest
	 * prefix and suffix, false otherwise.
	 * @param filename Name of the manifest file.
	 * @param bagConstants Contains names for constants associated with a bag.
	 * @return True if the filename matches the BagConstants tag manifest
	 * prefix and suffix, false otherwise.
	 */
	public static boolean isTagManifest(String filename, BagConstants bagConstants) {
		if (filename.startsWith(bagConstants.getTagManifestPrefix()) && filename.endsWith(bagConstants.getTagManifestSuffix())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the checksum algorithm used in a manifest file.
	 * @param filename Name of the manifest file.
	 * @param bagConstants Contains names for constants associated with a bag.
	 * @return A checksum algorithm.
	 */
	public static Algorithm getAlgorithm(String filename, BagConstants bagConstants)
	{
		String bagItAlgorithm;
		if (isPayloadManifest(filename, bagConstants)) {
			bagItAlgorithm =  filename.substring(bagConstants.getPayloadManifestPrefix().length(), filename.length()-bagConstants.getPayloadManifestSuffix().length());
		}
		else if (isTagManifest(filename, bagConstants)) {
			bagItAlgorithm =  filename.substring(bagConstants.getTagManifestPrefix().length(), filename.length()-bagConstants.getTagManifestSuffix().length());
		}
		else {
			throw new RuntimeException("Algorithm not found in manifest filename");	
		}
		Algorithm algorithm = Algorithm.valueOfBagItAlgorithm(bagItAlgorithm);
		log.debug(MessageFormat.format("Determined that algorithm for {0} is {1}.", filename, algorithm.toString()));
		return algorithm;
						
	}
	
	/**
	 * Returns the tag manifest filename.
	 * @param algorithm The algorithm used in a tag manifest file.
	 * @param bagConstants Contains names for constants associated with a bag.
	 * @return A tag manifest filename.
	 */
	public static String getTagManifestFilename(Algorithm algorithm, BagConstants bagConstants) {
		return bagConstants.getTagManifestPrefix() + algorithm.bagItAlgorithm + bagConstants.getTagManifestSuffix();
	}
	
	/**
	 * Returns the payload manifest filename.
	 * @param algorithm The algorithm used in a payload manifest file.
	 * @param bagConstants Contains names for constants associated with a bag.
	 * @return A payload manifest filename.
	 */
	public static String getPayloadManifestFilename(Algorithm algorithm, BagConstants bagConstants) {
		return bagConstants.getPayloadManifestPrefix() + algorithm.bagItAlgorithm + bagConstants.getPayloadManifestSuffix();
	}
	
}
