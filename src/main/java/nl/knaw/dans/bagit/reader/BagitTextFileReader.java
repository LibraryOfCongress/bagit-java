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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;

import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import nl.knaw.dans.bagit.domain.Version;
import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;
import nl.knaw.dans.bagit.exceptions.UnparsableVersionException;

/**
 * This class is responsible for reading and parsing bagit.txt files from the filesystem
 */
public final class BagitTextFileReader {
  private static final Logger logger = LoggerFactory.getLogger(BagitTextFileReader.class);
  private static final byte[] BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private static final Version VERSION_1_0 = new Version(1, 0);
  private static final String LINE1_REGEX = "(BagIt-Version: )\\d*\\.\\d*";
  private static final String LINE2_REGEX = "(Tag-File-Character-Encoding: )\\S*";
  
  private BagitTextFileReader(){
    //intentionally left empty
  }

  /**
   * Read the bagit.txt file and return the version and encoding.
   * 
   * @param bagitFile the bagit.txt file
   * @return the bag {@link Version} and {@link Charset} encoding of the tag files
   * 
   * @throws IOException if there is a problem reading a file. The file MUST be in UTF-8 encoding.
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
    
    final Version parsedVersion = parseVersion(version);
    if(parsedVersion.isSameOrNewer(VERSION_1_0)){
      final List<String> lines = Files.readAllLines(bagitFile, StandardCharsets.UTF_8);
      throwErrorIfLinesDoNotMatchStrict(lines);
    }
    
    return new SimpleImmutableEntry<>(parsedVersion, encoding);
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
   * As per the specification, if version is 1.0+ it must only contain 2 lines of the form
   * BagIt-Version: <M.N>
   * Tag-File-Character-Encoding: <ENCODING>
   */
  static void throwErrorIfLinesDoNotMatchStrict(final List<String> lines) throws InvalidBagitFileFormatException{
    if(lines.size() > 2){
      final List<String> offendingLines = lines.subList(2, lines.size()-1);
      throw new InvalidBagitFileFormatException(MessageFormatter
          .format(messages.getString("strict_only_two_lines_error"), offendingLines).getMessage());
    }
    if(!lines.get(0).matches(LINE1_REGEX)){
      throw new InvalidBagitFileFormatException(MessageFormatter
          .format(messages.getString("strict_first_line_error"), lines.get(0)).getMessage());
    }
    if(!lines.get(1).matches(LINE2_REGEX)){
      throw new InvalidBagitFileFormatException(MessageFormatter
          .format(messages.getString("strict_second_line_error"), lines.get(0)).getMessage());
    }
  }
  
  /*
   * parses the version string into a {@link Version} object
   */
  public static Version parseVersion(final String version) throws UnparsableVersionException{
    if(!version.contains(".")){
      throw new UnparsableVersionException(messages.getString("unparsable_version_error"), version);
    }
    
    final String[] parts = version.trim().split("\\.");
    if(parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()){
      throw new UnparsableVersionException(messages.getString("unparsable_version_error"), version);
    }
    
    final int major = Integer.parseInt(parts[0]);
    final int minor = Integer.parseInt(parts[1]);
    
    return new Version(major, minor);
  }
}
