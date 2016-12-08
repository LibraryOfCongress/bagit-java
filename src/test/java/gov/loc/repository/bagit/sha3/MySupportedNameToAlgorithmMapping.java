package gov.loc.repository.bagit.sha3;

import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;

public class MySupportedNameToAlgorithmMapping implements BagitAlgorithmNameToSupportedAlgorithmMapping {

  @Override
  public SupportedAlgorithm getSupportedAlgorithm(String bagitAlgorithmName) {
    if("sha3256".equals(bagitAlgorithmName)){
      return new SHA3256Algorithm();
    }

    return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase());
  }

}
