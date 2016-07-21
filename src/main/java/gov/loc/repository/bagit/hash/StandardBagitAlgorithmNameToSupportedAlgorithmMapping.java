package gov.loc.repository.bagit.hash;

import java.util.Locale;

/**
 * Provides a mapping between bagit algorithm names and {@link SupportedAlgorithm} 
 */
public class StandardBagitAlgorithmNameToSupportedAlgorithmMapping
    implements BagitAlgorithmNameToSupportedAlgorithmMapping {

  @Override
  public SupportedAlgorithm getMessageDigestName(final String bagitAlgorithmName) {
    return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase(Locale.getDefault()));
  }
}
