package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Version;

public final class MetadataWriter {
  private static final Logger logger = LoggerFactory.getLogger(MetadataWriter.class);
  private static final Version VERSION_0_95 = new Version(0, 95);

  private MetadataWriter(){
    //intentionall left empty
  }
  
  /**
   * Write the bag-info.txt (or package-info.txt) file to the specified outputDir with specified encoding (charsetName)
   * 
   * @param metadata the key value pair info in the bag-info.txt file
   * @param version the version of the bag you are writing
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writeBagMetadata(final List<SimpleImmutableEntry<String, String>> metadata, final Version version, final Path outputDir, final Charset charsetName) throws IOException{
    Path bagInfoFilePath = outputDir.resolve("bag-info.txt");
    if(VERSION_0_95.compareTo(version) >= 0){
      bagInfoFilePath = outputDir.resolve("package-info.txt");
    }
    logger.debug("Writing {} to [{}]", bagInfoFilePath.getFileName(), outputDir);

    Files.deleteIfExists(bagInfoFilePath);
    
    for(final SimpleImmutableEntry<String, String> entry : metadata){
      final String line = entry.getKey() + " : " + entry.getValue() + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, bagInfoFilePath);
      Files.write(bagInfoFilePath, line.getBytes(charsetName), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
}
