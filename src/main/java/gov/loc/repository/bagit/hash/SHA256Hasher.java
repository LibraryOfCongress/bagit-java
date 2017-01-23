package gov.loc.repository.bagit.hash;

import java.security.NoSuchAlgorithmException;

public final class SHA256Hasher extends AbstractMessageDigestHasher {
  
  public SHA256Hasher() throws NoSuchAlgorithmException{
    super("SHA-256", "sha256");
  }

}
