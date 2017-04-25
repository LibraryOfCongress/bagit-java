package gov.loc.repository.bagit.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;

/**
 * This class is responsible for reading and parsing bagit metadata files from the filesystem
 */
public final class MetadataReader {
  private static final Logger logger = LoggerFactory.getLogger(MetadataReader.class);

  private MetadataReader(){
    //intentionally left empty
  }
  
  /**
   * Reads the bag metadata file (bag-info.txt or package-info.txt) and returns it.
   * 
   * @param rootDir the root directory of the bag
   * @param encoding the encoding of the bagit.txt file
   * @return the bag-info.txt (metadata) information
   * 
   * @throws IOException if there is a problem reading a file
   * @throws InvalidBagMetadataException if the metadata file does not conform to the bagit spec
   */
  public static List<SimpleImmutableEntry<String, String>> readBagMetadata(final Path rootDir, final Charset encoding) throws IOException, InvalidBagMetadataException{
    logger.info("Attempting to read bag metadata file");
    List<SimpleImmutableEntry<String, String>> metadata = new ArrayList<>();
    
    final Path bagInfoFile = rootDir.resolve("bag-info.txt");
    if(Files.exists(bagInfoFile)){
      logger.debug("Found [{}] file", bagInfoFile);
      metadata = KeyValueReader.readKeyValuesFromFile(bagInfoFile, ":", encoding);
    }
    final Path packageInfoFile = rootDir.resolve("package-info.txt"); //only exists in versions 0.93 - 0.95
    if(Files.exists(packageInfoFile)){
      logger.debug("Found [{}] file", packageInfoFile);
      metadata = KeyValueReader.readKeyValuesFromFile(packageInfoFile, ":", encoding);
    }
    
    return metadata;
  }
}
