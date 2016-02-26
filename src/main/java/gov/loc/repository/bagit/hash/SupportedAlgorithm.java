package gov.loc.repository.bagit.hash;

import java.security.MessageDigest;

/**
 * Easy way to see which algorithms are supported and convert between bagit manifest spec and {@link MessageDigest}<br>
 * See {@link StandardSupportedAlgorithms} for a nice list of defaults
 */
public interface SupportedAlgorithm {
  public String getMessageDigestName();
  public String getBagitName();
}
