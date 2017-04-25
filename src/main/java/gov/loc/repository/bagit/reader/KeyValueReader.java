package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
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
 * Convenience class for reading key value pairs from a file
 */
public final class KeyValueReader {
  private static final Logger logger = LoggerFactory.getLogger(KeyValueReader.class);

  private KeyValueReader(){
    //intentionall left blank
  }
  

  /**
   * Generic method to read key value pairs from the bagit files, like bagit.txt or bag-info.txt
   * 
   * @param file the file to read
   * @param splitRegex how to split the key from the value
   * @param charset the encoding of the file
   * 
   * @return a list of key value pairs
   * 
   * @throws IOException if there was a problem reading the file
   * @throws InvalidBagMetadataException if the file does not conform to pattern of key value
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public static List<SimpleImmutableEntry<String, String>> readKeyValuesFromFile(final Path file, final String splitRegex, final Charset charset) throws IOException, InvalidBagMetadataException{
    final List<SimpleImmutableEntry<String, String>> keyValues = new ArrayList<>();
    
    try(final BufferedReader reader = Files.newBufferedReader(file, charset)){
      String line = reader.readLine();
      while(line != null){
        if(line.matches("^\\s+.*") && !keyValues.isEmpty()){
          mergeIndentedLine(line, keyValues);
        }
        else{
          final String[] parts = checkLineFormat(line, splitRegex);
          final String key = parts[0].trim();
          final String value = parts[1].trim();
          logger.debug("Found key [{}] value [{}] in file [{}] using split regex [{}]", key, value, file, splitRegex);
          keyValues.add(new SimpleImmutableEntry<>(key, value));
        }
         
        line = reader.readLine();
      }
    }
    
    return keyValues;
  }
  
  private static void mergeIndentedLine(final String line, final List<SimpleImmutableEntry<String, String>> keyValues){
    final SimpleImmutableEntry<String, String> oldKeyValue = keyValues.remove(keyValues.size() -1);
    final SimpleImmutableEntry<String, String> newKeyValue = new SimpleImmutableEntry<>(oldKeyValue.getKey(), oldKeyValue.getValue() + System.lineSeparator() +line);
    keyValues.add(newKeyValue);
    
    logger.debug("Found an indented line - merging it with key [{}]", oldKeyValue.getKey());
  }
  
  private static String[] checkLineFormat(final String line, final String splitRegex) throws InvalidBagMetadataException{
    final String[] parts = line.split(splitRegex, 2);
    
    if(parts.length != 2){
      final StringBuilder message = new StringBuilder(300);
      message.append("Line [").append(line)
        .append("] does not meet the bagit specification for a bag tag file. Perhaps you meant to indent it " +
        "by a space or a tab? Or perhaps you didn't use a colon to separate the key from the value?" +
        "It must follow the form of <key>")
        .append(splitRegex)
        .append("<value> or if continuing from another line must be indented by a space or a tab.");
      
      throw new InvalidBagMetadataException(message.toString());
    }
    
    return parts;
  }
}
