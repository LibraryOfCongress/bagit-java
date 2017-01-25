package gov.loc.repository.bagit.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;

public final class BagitTextFileReader {
  private static final Logger logger = LoggerFactory.getLogger(BagitTextFileReader.class);
  
  private BagitTextFileReader(){
    //intentionally left empty
  }

  /**
   * Read the bagit.txt file and return the version and encoding.
   * 
   * @param bagitFile the bagit.txt file
   * @return the bag {@link Version} and {@link Charset} encoding of the tag files
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public static SimpleImmutableEntry<Version, Charset> readBagitTextFile(final Path bagitFile) throws IOException, UnparsableVersionException, InvalidBagMetadataException{
    final BagitFileValues values = parseValues(bagitFile);
    
    return new SimpleImmutableEntry<Version, Charset>(values.getVersion(), values.getEncoding());
  }
  
  /**
   * Read the Payload-Byte-Count and Payload-File-Count from the bagit.txt file
   * @since bagic specification 1.0
   * 
   * @param bagitFile the bagit.txt file to read
   * 
   * @return the payload byte count, payload file count (in that order)
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public static SimpleImmutableEntry<Long, Long> readPayloadByteAndFileCount(final Path bagitFile) throws UnparsableVersionException, IOException, InvalidBagMetadataException{
    final BagitFileValues values = parseValues(bagitFile);
    
    return new SimpleImmutableEntry<Long, Long>(values.getPayloadByteCount(), values.getPayloadFileCount());
  }
  
  /**
   * Read version, file encoding, and (possibly) payload byte and file count
   * 
   * @param bagitFile the bagit.txt file to read
   * 
   * @return all the possible bagit.txt file field values
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public static BagitFileValues parseValues(final Path bagitFile) throws UnparsableVersionException, IOException, InvalidBagMetadataException{
    logger.debug("Reading [{}] file", bagitFile);
    final List<SimpleImmutableEntry<String, String>> pairs = KeyValueReader.readKeyValuesFromFile(bagitFile, ":", StandardCharsets.UTF_8);
    
    
    Version version = null;
    Charset encoding = StandardCharsets.UTF_8;
    Long payloadByteCount = null;
    Long payloadFileCount = null;
    
    for(final SimpleImmutableEntry<String, String> pair : pairs){
      if("BagIt-Version".equals(pair.getKey())){
        version = parseVersion(pair.getValue());
      }
      if("Tag-File-Character-Encoding".equals(pair.getKey())){
        encoding = Charset.forName(pair.getValue());
      }
      if("Payload-Byte-Count".equals(pair.getKey())){ //assume version is 1.0+
        payloadByteCount = Long.valueOf(pair.getValue());
      }
      if("Payload-File-Count".equals(pair.getKey())){ //assume version is 1.0+
        payloadFileCount = Long.valueOf(pair.getValue());
      }
      logger.debug("[{}] is [{}]", pair.getKey(), pair.getValue());
    }
    
    return new BagitFileValues(version, encoding, payloadByteCount, payloadFileCount);
  }
  
  /*
   * parses the version string into a {@link Version} object
   */
  static Version parseVersion(final String version) throws UnparsableVersionException{
    if(!version.contains(".")){
      throw new UnparsableVersionException("Version must be in format MAJOR.MINOR but was " + version);
    }
    
    final String[] parts = version.split("\\.");
    final int major = Integer.parseInt(parts[0]);
    final int minor = Integer.parseInt(parts[1]);
    
    return new Version(major, minor);
  }
}
