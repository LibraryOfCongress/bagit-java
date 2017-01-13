package gov.loc.repository.bagit.hash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Manifest;

/**
 * Convenience class for generating a HEX formatted string of the checksum hash. 
 */
public final class HasherOld {
  private static final Logger logger = LoggerFactory.getLogger(HasherOld.class);
  private static final int _64_KB = 1024 * 64;
  private static final int CHUNK_SIZE = _64_KB;
  
  private HasherOld(){
    //intentionally left empty
  }
  
  /**
   * Create a HEX formatted string checksum hash of the file
   * 
   * @param path the {@link Path} (file) to hash
   * @param messageDigest the {@link MessageDigest} object representing the hashing algorithm
   * @return the hash as a hex formated string
   * @throws IOException if there is a problem reading the file
   */
  public static String hash(final Path path, final MessageDigest messageDigest) throws IOException {
    updateMessageDigests(path, Arrays.asList(messageDigest));
    
    return formatMessageDigest(messageDigest);
  }
  
  /**
   * Update the Manifests with the file's hash
   * 
   * @param path the {@link Path} (file) to hash
   * @param manifestToMessageDigestMap the map between {@link Manifest} and {@link MessageDigest}
   * @throws IOException if there is a problem reading the file
   */
  public static void hash(final Path path, final Map<Manifest, MessageDigest> manifestToMessageDigestMap) throws IOException {
    updateMessageDigests(path, manifestToMessageDigestMap.values());
    addMessageDigestHashToManifest(path, manifestToMessageDigestMap);
  }
  
  private static void updateMessageDigests(final Path path, final Collection<MessageDigest> messageDigests) throws IOException{
    try(final InputStream is = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))){
      final byte[] buffer = new byte[CHUNK_SIZE];
      int read = is.read(buffer);
      
      while(read != -1) {
        for(final MessageDigest messageDigest : messageDigests){
          messageDigest.update(buffer, 0, read);
        }
        read = is.read(buffer);
      }
    }
  }
  
  private static void addMessageDigestHashToManifest(final Path path, final Map<Manifest, MessageDigest> manifestToMessageDigestMap){
    for(final Entry<Manifest, MessageDigest> entry : manifestToMessageDigestMap.entrySet()){
      final String hash = formatMessageDigest(entry.getValue());
      logger.debug("Adding [{}] to manifest with hash [{}]", path, hash);
      entry.getKey().getFileToChecksumMap().put(path, hash);
    }
  }
  
  //Convert the byte to hex format
  private static String formatMessageDigest(final MessageDigest messageDigest){
    final Formatter formatter = new Formatter();
    
    for (final byte b : messageDigest.digest()) {
      formatter.format("%02x", b);
    }
    
    final String hash = formatter.toString();
    formatter.close();
    
    return hash;
  }
  
  /**
   * create a mapping between {@link Manifest} and {@link MessageDigest} for each each supplied {@link SupportedAlgorithm} 
   * 
   * @param algorithms the {@link SupportedAlgorithm} that you which to map to {@link MessageDigest} 
   * @return mapping between {@link Manifest} and {@link MessageDigest}
   * @throws NoSuchAlgorithmException if {@link MessageDigest} doesn't support the algorithm
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public static Map<Manifest, MessageDigest> createManifestToMessageDigestMap(final Collection<SupportedAlgorithm> algorithms) throws NoSuchAlgorithmException{
    final Map<Manifest, MessageDigest> map = new HashMap<>();

    for(final SupportedAlgorithm algorithm : algorithms){
      final MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
      final Manifest manifest = new Manifest(algorithm);
      map.put(manifest, messageDigest);
    }
    
    return map;
  }
}
