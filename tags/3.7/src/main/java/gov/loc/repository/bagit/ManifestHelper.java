package gov.loc.repository.bagit;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.Bag.BagConstants;

public class ManifestHelper {
	
	private static final Log log = LogFactory.getLog(ManifestHelper.class);
	
	public static boolean isPayloadManifest(String filename, BagConstants bagConstants) {
		if (filename.startsWith(bagConstants.getPayloadManifestPrefix()) && filename.endsWith(bagConstants.getPayloadManifestSuffix())) {
			return true;
		}
		return false;
	}

	public static boolean isTagManifest(String filename, BagConstants bagConstants) {
		if (filename.startsWith(bagConstants.getTagManifestPrefix()) && filename.endsWith(bagConstants.getTagManifestSuffix())) {
			return true;
		}
		return false;
	}
	
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
	
	public static String getTagManifestFilename(Algorithm algorithm, BagConstants bagConstants) {
		return bagConstants.getTagManifestPrefix() + algorithm.bagItAlgorithm + bagConstants.getTagManifestSuffix();
	}
	
	public static String getPayloadManifestFilename(Algorithm algorithm, BagConstants bagConstants) {
		return bagConstants.getPayloadManifestPrefix() + algorithm.bagItAlgorithm + bagConstants.getPayloadManifestSuffix();
	}
	
}
