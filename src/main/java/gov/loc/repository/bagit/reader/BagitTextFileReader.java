package gov.loc.repository.bagit.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;

/**
 * This class is responsible for reading and parsing bagit.txt files from the filesystem
 */
public final class BagitTextFileReader {
  private static final Logger logger = LoggerFactory.getLogger(BagitTextFileReader.class);
  private static final byte[] BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
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
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to "key: value"
   * @throws InvalidBagitFileFormatException if the bagit.txt file does not conform to the bagit spec
   */
  public static SimpleImmutableEntry<Version, Charset> readBagitTextFile(final Path bagitFile) throws IOException, UnparsableVersionException, InvalidBagMetadataException, InvalidBagitFileFormatException{
    logger.debug(messages.getString("reading_version_and_encoding"), bagitFile);
    throwErrorIfByteOrderMarkIsPresent(bagitFile);
    final List<SimpleImmutableEntry<String, String>> pairs = KeyValueReader.readKeyValuesFromFile(bagitFile, ":", StandardCharsets.UTF_8);
    
    String version = null;
    Charset encoding = null;
    for(final SimpleImmutableEntry<String, String> pair : pairs){
      if("BagIt-Version".equals(pair.getKey())){
        version = pair.getValue();
        logger.debug(messages.getString("bagit_version"), version);
      }
      if("Tag-File-Character-Encoding".equals(pair.getKey())){
        encoding = Charset.forName(pair.getValue());
        logger.debug(messages.getString("tag_file_encoding"), encoding);
      }
    }
    
    if(version == null || encoding == null){
      throw new InvalidBagitFileFormatException(messages.getString("invalid_bagit_text_file_error"));
    }
    
    return new SimpleImmutableEntry<>(parseVersion(version), encoding);
  }
  
  /*
   * As per the specification, a BOM is not allowed in the bagit.txt file
   */
  private static void throwErrorIfByteOrderMarkIsPresent(final Path bagitFile) throws IOException, InvalidBagitFileFormatException{
    final byte[] firstFewBytesInFile = Arrays.copyOfRange(Files.readAllBytes(bagitFile), 0, BOM.length);
    if(Arrays.equals(BOM, firstFewBytesInFile)){
      final String formattedMessage = messages.getString("bom_present_error");
      throw new InvalidBagitFileFormatException(MessageFormatter.format(formattedMessage, bagitFile).getMessage());
    }
  }
  
  /*
   * parses the version string into a {@link Version} object
   */
  public static Version parseVersion(final String version) throws UnparsableVersionException{
    if(!version.contains(".")){
      throw new UnparsableVersionException(messages.getString("unparsable_version_error"), version);
    }
    
    final String[] parts = version.split("\\.");
    final int major = Integer.parseInt(parts[0]);
    final int minor = Integer.parseInt(parts[1]);
    
    return new Version(major, minor);
  }
}
