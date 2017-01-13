package gov.loc.repository.bagit.hash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public interface Hasher {
  int _64_KB = 1024 * 64;
  int CHUNK_SIZE = _64_KB;

  /**
   * Update the computed checksum with more data
   * @param buffer which stores the bytes
   * @parm length how much of the buffer to use when updating the checksum
   */
  void update(final byte[] buffer, final int length);
  
  /**
   * Clear the current calculated checksum value
   */
  void clear();
  
  /**
   * @return the calculated checksum value as a HEX formated string
   */
  String value();
  
  /**
   * @return the lower-case, non-hyphenated name of the checksum algorithm used.
   * Example SHA-512 becomes sha512
   */
  String getBagitName();
  
  default void hashSingleFile(final Path path) throws IOException{
    try(final InputStream is = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))){
      final byte[] buffer = new byte[CHUNK_SIZE];
      int read = is.read(buffer);
      
      while(read != -1) {
        this.update(buffer, read);
        read = is.read(buffer);
      }
    }
  }
}
