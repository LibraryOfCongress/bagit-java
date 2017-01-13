package gov.loc.repository.bagit.hash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;

public interface Hasher extends Cloneable{
  int _64_KB = 1024 * 64;
  int CHUNK_SIZE = _64_KB;

  /**
   * Update the computed checksum with more data
   * @param buffer which stores the bytes
   * @param length how much of the buffer to use when updating the checksum
   */
  void update(final byte[] buffer, final int length);
  
  /**
   * Clear the current calculated checksum value
   */
  void clear();
  
  /**
   * @return the calculated checksum value as a HEX formated string
   */
  String getCalculatedValue();
  
  /**
   * @return the lower-case, non-hyphenated name of the checksum algorithm used.
   * Example SHA-512 becomes sha512
   */
  String getBagitName();
  
  /**
   * @return  a new instance of the implemented hasher
   * @throws UnsupportedAlgorithmException if there was a problem creating a new instance.
   */
  Hasher instanceOf() throws UnsupportedAlgorithmException;
  
  /**
   * calculate the checksum for a single file all at once.
   * 
   * @param path the file to calculate the checksum on
   * @throws IOException if there is a problem reading the file
   */
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
