package gov.loc.repository.bagit.hash;

import java.util.Locale;
import java.util.ResourceBundle;

import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;

/**
 * Provides a mapping between bagit algorithm names and {@link SupportedAlgorithm} 
 */
public class StandardBagitAlgorithmNameToSupportedAlgorithmMapping
    implements BagitAlgorithmNameToSupportedAlgorithmMapping {
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

  @Override
  public SupportedAlgorithm getSupportedAlgorithm(final String bagitAlgorithmName) throws UnsupportedAlgorithmException {
    try{
      return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase(Locale.getDefault()));
    }
    catch(IllegalArgumentException e){
      throw new UnsupportedAlgorithmException(messages.getString("algorithm_not_supported_error"), bagitAlgorithmName, e);
    }
  }
}
