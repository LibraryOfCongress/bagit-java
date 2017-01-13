package gov.loc.repository.bagit.hash;

import java.security.NoSuchAlgorithmException;

public final class SHA1Hasher extends AbstractMessageDigestHasher {
  
  public SHA1Hasher() throws NoSuchAlgorithmException{
    super("SHA-1", "sha1");
  }

}
