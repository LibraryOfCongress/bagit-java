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
  
  /**
   * Create a HEX formatted string of the checksum hash
   * 
   * @param inputStream the stream that you wish to hash
   * @param messageDigest the {@link MessageDigest} object representing the hashing algorithm
   * @return the hash as a hex formated string
   * @throws IOException if there is a problem reading the file
   */
  public static String hash(final InputStream inputStream, final MessageDigest messageDigest) throws IOException {
    InputStream is = new BufferedInputStream(inputStream);
    final byte[] buffer = new byte[1024];
    
    int read = is.read(buffer);
    
    while(read != -1) {
      messageDigest.update(buffer, 0, read);
      read = is.read(buffer);
    }
    
    return formatMessageDigest(messageDigest);
  }
  
  //Convert the byte to hex format
  protected static String formatMessageDigest(final MessageDigest messageDigest){
    Formatter formatter = new Formatter();
    
    for (final byte b : messageDigest.digest()) {
      formatter.format("%02x", b);
    }
    
    String hash = formatter.toString();
    formatter.close();
    
    return hash;
  }
}
