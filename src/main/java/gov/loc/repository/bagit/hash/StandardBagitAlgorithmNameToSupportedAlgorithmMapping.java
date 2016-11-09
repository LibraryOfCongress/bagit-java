package gov.loc.repository.bagit.hash;

import java.util.Locale;

import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;

/**
 * Provides a mapping between bagit algorithm names and {@link SupportedAlgorithm} 
 */
public class StandardBagitAlgorithmNameToSupportedAlgorithmMapping
    implements BagitAlgorithmNameToSupportedAlgorithmMapping {

  @Override
  public SupportedAlgorithm getMessageDigestName(final String bagitAlgorithmName) throws UnsupportedAlgorithmException {
    try{
      return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase(Locale.getDefault()));
    }
    catch(IllegalArgumentException e){
      throw new UnsupportedAlgorithmException(bagitAlgorithmName + " is not supported!", e);
    }
  }
}
