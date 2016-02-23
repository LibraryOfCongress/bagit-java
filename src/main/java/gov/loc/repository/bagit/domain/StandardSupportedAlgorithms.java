package gov.loc.repository.bagit.domain;

import java.security.MessageDigest;

/**
 * Easy way to see which algorithms are supported and convert between bagit manifest spec and {@link MessageDigest}
 */
public enum StandardSupportedAlgorithms implements SupportedAlgorithm{
  MD5("MD5"),
  SHA1("SHA-1"),
  SHA256("SHA-256"),
  SHA512("SHA-512");

  private final String messageDigestName;
  
  private StandardSupportedAlgorithms(String messageDigestName){
    this.messageDigestName = messageDigestName;
  }

  @Override
  public String getMessageDigestName() {
    return messageDigestName;
  }

  @Override
  public String getBagitName() {
    return name();
  }
}
