package gov.loc.repository.bagit.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;

public final class BagitTextFileReader {
  private static final Logger logger = LoggerFactory.getLogger(BagitTextFileReader.class);
  private static final byte[] BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
  
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
   * @throws InvalidBagitFileFormatException if the bagit.txt file does not conform to the bagit spec
   */
  public static SimpleImmutableEntry<Version, Charset> readBagitTextFile(final Path bagitFile) throws IOException, UnparsableVersionException, InvalidBagMetadataException, InvalidBagitFileFormatException{
    logger.debug("Reading [{}] file", bagitFile);
    throwErrorIfByteOrderMarkIsPresent(bagitFile);
    final List<SimpleImmutableEntry<String, String>> pairs = KeyValueReader.readKeyValuesFromFile(bagitFile, ":", StandardCharsets.UTF_8);
    
    String version = "";
    Charset encoding = StandardCharsets.UTF_8;
    for(final SimpleImmutableEntry<String, String> pair : pairs){
      if("BagIt-Version".equals(pair.getKey())){
        version = pair.getValue();
        logger.debug("BagIt-Version is [{}]", version);
      }
      if("Tag-File-Character-Encoding".equals(pair.getKey())){
        encoding = Charset.forName(pair.getValue());
        logger.debug("Tag-File-Character-Encoding is [{}]", encoding);
      }
    }
    
    return new SimpleImmutableEntry<Version, Charset>(parseVersion(version), encoding);
  }
  
  /*
   * As per the specification, a BOM is not allowed in the bagit.txt file
   */
  private static void throwErrorIfByteOrderMarkIsPresent(final Path bagitFile) throws IOException, InvalidBagitFileFormatException{
    byte[] firstFewBytesInFile = Arrays.copyOfRange(Files.readAllBytes(bagitFile), 0, BOM.length);
    if(Arrays.equals(BOM, firstFewBytesInFile)){
      throw new InvalidBagitFileFormatException("File [" + bagitFile + "] contains a byte order mark (BOM) which "
          + "is not allowed by the bagit specification!");
    }
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
