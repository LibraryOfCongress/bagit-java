package gov.loc.repository.bagit.hash;

import java.security.NoSuchAlgorithmException;

public final class SHA512Hasher extends AbstractMessageDigestHasher {
  
  public SHA512Hasher() throws NoSuchAlgorithmException{
    super("SHA-512", "sha512");
  }

}
