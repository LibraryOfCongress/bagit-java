package gov.loc.repository.bagit.verify;

import java.security.NoSuchAlgorithmException;

import gov.loc.repository.bagit.hash.AbstractMessageDigestHasher;

public class SHA3Hasher extends AbstractMessageDigestHasher {
  public SHA3Hasher() throws NoSuchAlgorithmException{
    super("SHA3-256", "sha3256");
  }
}
