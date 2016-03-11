package gov.loc.repository.bagit.hash;

/**
 * Implement this interface if you need to be able to use other algorithms than the {@link StandardSupportedAlgorithms}
 */
public interface BagitAlgorithmNameToSupportedAlgorithmMapping {
  public SupportedAlgorithm getMessageDigestName(String bagitAlgorithmName);
}
