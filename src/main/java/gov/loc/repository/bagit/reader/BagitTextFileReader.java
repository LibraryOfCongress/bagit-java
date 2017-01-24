package gov.loc.repository.bagit.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
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
    
    return new SimpleImmutableEntry<Version, Charset>(values.version, values.encoding);
  }
  
  /**
   * Read the bagit.txt file and get the version and encoding. In version 1.0+ also check for
   * payload-byte-count and payload-file-count
   * 
   * @param bag the to read that contains the bagit.txt file and set the values in the bag
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public static void readBagitTextFile(final Bag bag) throws IOException, UnparsableVersionException, InvalidBagMetadataException{
    Path bagitDir = bag.getRootDir().resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = bag.getRootDir();
    }
    final BagitFileValues values = parseValues(bagitDir.resolve("bagit.txt"));
    
    bag.setVersion(values.version);
    bag.setFileEncoding(values.encoding);
    bag.setPayloadByteCount(values.payloadByteCount);
    bag.setPayloadFileCount(values.payloadFileCount);
  }
  
  private static BagitFileValues parseValues(final Path bagitFile) throws UnparsableVersionException, IOException, InvalidBagMetadataException{
    logger.debug("Reading [{}] file", bagitFile);
    final List<SimpleImmutableEntry<String, String>> pairs = KeyValueReader.readKeyValuesFromFile(bagitFile, ":", StandardCharsets.UTF_8);
    final BagitFileValues values = new BagitFileValues();
    
    String version = "";
    Charset encoding = StandardCharsets.UTF_8;
    for(final SimpleImmutableEntry<String, String> pair : pairs){
      if("BagIt-Version".equals(pair.getKey())){
        version = pair.getValue();
        values.version = parseVersion(version);
      }
      if("Tag-File-Character-Encoding".equals(pair.getKey())){
        encoding = Charset.forName(pair.getValue());
        values.encoding = encoding;
      }
      if("Payload-Byte-Count".equals(pair.getKey())){ //assume version is 1.0+
        values.payloadByteCount = Long.valueOf(pair.getValue());
      }
      if("Payload-File-Count".equals(pair.getKey())){ //assume version is 1.0+
        values.payloadFileCount = Long.valueOf(pair.getValue());
      }
      logger.debug("[{}] is [{}]", pair.getKey(), pair.getValue());
    }
    
    return values;
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
  
  @SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
  private static class BagitFileValues{
    public Version version;
    public Charset encoding;
    public Long payloadByteCount;
    public Long payloadFileCount;
  }
}
