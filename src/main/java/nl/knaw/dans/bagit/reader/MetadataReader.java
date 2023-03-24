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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;

/**
 * This class is responsible for reading and parsing bagit metadata files from the filesystem
 */
public final class MetadataReader {
  private static final Logger logger = LoggerFactory.getLogger(MetadataReader.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

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
    logger.info(messages.getString("attempting_read_metadata"));
    List<SimpleImmutableEntry<String, String>> metadata = new ArrayList<>();
    
    final Path bagInfoFile = rootDir.resolve("bag-info.txt");
    if(Files.exists(bagInfoFile)){
      logger.debug(messages.getString("found_metadata_file"), bagInfoFile);
      metadata = KeyValueReader.readKeyValuesFromFile(bagInfoFile, ":", encoding);
    }
    final Path packageInfoFile = rootDir.resolve("package-info.txt"); //only exists in versions 0.93 - 0.95
    if(Files.exists(packageInfoFile)){
      logger.debug(messages.getString("found_metadata_file"), packageInfoFile);
      metadata = KeyValueReader.readKeyValuesFromFile(packageInfoFile, ":", encoding);
    }
    
    return metadata;
  }
}
