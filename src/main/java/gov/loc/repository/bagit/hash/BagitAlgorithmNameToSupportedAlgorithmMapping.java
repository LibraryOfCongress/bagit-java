package gov.loc.repository.bagit.hash;

import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;

/**
 * Implement this interface if you need to be able to use other algorithms than the {@link StandardSupportedAlgorithms}
 */
public interface BagitAlgorithmNameToSupportedAlgorithmMapping {
  SupportedAlgorithm getMessageDigestName(String bagitAlgorithmName) throws UnsupportedAlgorithmException;
}
