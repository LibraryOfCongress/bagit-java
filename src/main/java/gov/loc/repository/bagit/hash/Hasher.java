package gov.loc.repository.bagit.hash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Formatter;

/**
 * Convenience class for generating a HEX formatted string of the checksum hash. 
 */
public class Hasher {
  
  public static String hash(final InputStream inputStream, final MessageDigest messageDigest) throws IOException {
    try (InputStream is = new BufferedInputStream(inputStream)) {
      final byte[] buffer = new byte[1024];
      for (int read = 0; (read = is.read(buffer)) != -1;) {
        messageDigest.update(buffer, 0, read);
      }
    }

    // Convert the byte to hex format
    return formatMessageDigest(messageDigest);
  }
  
  protected static String formatMessageDigest(final MessageDigest messageDigest){
    try (Formatter formatter = new Formatter()) {
      for (final byte b : messageDigest.digest()) {
        formatter.format("%02x", b);
      }
      return formatter.toString();
    }
  }
}
