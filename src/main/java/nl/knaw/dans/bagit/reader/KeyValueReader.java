/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;

/**
 * Convenience class for reading key value pairs from a file
 */
public final class KeyValueReader {
  private static final Logger logger = LoggerFactory.getLogger(KeyValueReader.class);
  private static final String INDENTED_LINE_REGEX = "^\\s+.*";
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

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
        if(line.matches(INDENTED_LINE_REGEX) && !keyValues.isEmpty()){
          mergeIndentedLine(line, keyValues);
        }
        else{
          final String[] parts = checkLineFormat(line, splitRegex);
          final String key = parts[0].trim();
          final String value = parts[1].trim();
          logger.debug(messages.getString("read_key_value_line"), key, value, file, splitRegex);
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
    
    logger.debug(messages.getString("found_indented_line"), oldKeyValue.getKey());
  }
  
  private static String[] checkLineFormat(final String line, final String splitRegex) throws InvalidBagMetadataException{
    final String[] parts = line.split(splitRegex, 2);
    
    if(parts.length != 2){
      final String formattedMessage = messages.getString("malformed_key_value_line_error");
      throw new InvalidBagMetadataException(MessageFormatter.format(formattedMessage, line, splitRegex).getMessage());
    }
    
    return parts;
  }
}
