package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.hash.SupportedAlgorithm;

public class SHA3256Algorithm implements SupportedAlgorithm {

  @Override
  public String getMessageDigestName() {
    return "SHA3-256";
  }

  @Override
  public String getBagitName() {
    return "sha3256";
  }

}
