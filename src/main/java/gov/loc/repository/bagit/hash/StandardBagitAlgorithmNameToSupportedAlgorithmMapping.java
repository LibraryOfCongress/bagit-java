package gov.loc.repository.bagit.hash;

/**
 * Provides a mapping between bagit algorithm names and {@link SupportedAlgorithm} 
 */
public class StandardBagitAlgorithmNameToSupportedAlgorithmMapping
    implements BagitAlgorithmNameToSupportedAlgorithmMapping {

  @Override
  public SupportedAlgorithm getMessageDigestName(String bagitAlgorithmName) {
    return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase());
  }
}
