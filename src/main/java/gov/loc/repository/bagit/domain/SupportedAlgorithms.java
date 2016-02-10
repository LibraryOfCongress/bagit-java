package gov.loc.repository.bagit.domain;

import java.security.MessageDigest;

/**
 * Easy way to see which algorithms are supported and convert between bagit manifest spec and {@link MessageDigest}
 */
public enum SupportedAlgorithms {
  MD5("MD5"),
  SHA1("SHA-1"),
  SHA256("SHA-256"),
  SHA512("SHA-512"),
  SHA3("SHA3-256");

  private final String messageDigestName;
  
  private SupportedAlgorithms(String messageDigestName){
    this.messageDigestName = messageDigestName;
  }

  public String getMessageDigestName() {
    return messageDigestName;
  }
}
