package gov.loc.repository.bagit.hash;

import java.security.NoSuchAlgorithmException;

public final class MD5Hasher extends AbstractMessageDigestHasher {
  
  public MD5Hasher() throws NoSuchAlgorithmException{
    super("MD5", "md5");
  }

}
