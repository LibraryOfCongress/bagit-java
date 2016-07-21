package gov.loc.repository.bagit.hash;

import java.security.MessageDigest;

/**
 * Easy way to convert between bagit manifest spec and {@link MessageDigest}<br>
 * See {@link StandardSupportedAlgorithms} for a list of defaults
 */
public interface SupportedAlgorithm {
  String getMessageDigestName();
  String getBagitName();
}
