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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.dans.bagit.domain.FetchItem;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;

/**
 * This class is responsible for reading and parsing fetch.txt file from the filesystem
 */
public final class FetchReader {
  private static final Logger logger = LoggerFactory.getLogger(FetchReader.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private static final String FETCH_LINE_REGEX = ".*[ \t]*(\\d*|-)[ \t]*.*";

  private FetchReader(){
    //intentionally left empty
  }
  
  /**
   * Reads a fetch.txt file
   * 
   * @param fetchFile the specific fetch file
   * @param encoding the encoding to read the file with
   * @param bagRootDir the root directory of the bag
   * @return a list of items to fetch
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousPathException if the path was crafted to point outside the bag directory
   * @throws InvalidBagitFileFormatException if the fetch format does not follow the bagit specification
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public static List<FetchItem> readFetch(final Path fetchFile, final Charset encoding, final Path bagRootDir) throws IOException, MaliciousPathException, InvalidBagitFileFormatException{
    logger.info(messages.getString("reading_fetch_file"), fetchFile);
    final List<FetchItem> itemsToFetch = new ArrayList<>();
    
    try(final BufferedReader reader = Files.newBufferedReader(fetchFile, encoding)){
      String line = reader.readLine();
      String[] parts = null;
      long length = 0;
      URL url = null;
      while(line != null){
        if(line.matches(FETCH_LINE_REGEX) && !line.matches("\\s*")){
          parts = line.split("\\s+", 3);
          final Path path = TagFileReader.createFileFromManifest(bagRootDir, parts[2]);
          length = parts[1].equals("-") ? -1 : Long.decode(parts[1]);
          url = new URL(parts[0]);
          
          logger.debug(messages.getString("read_fetch_file_line"), url, length, parts[2], fetchFile);
          final FetchItem itemToFetch = new FetchItem(url, length, path);
          itemsToFetch.add(itemToFetch);
        }
        else{
          throw new InvalidBagitFileFormatException(messages.getString("invalid_fetch_file_line_error").replace("{}", line));
        }
        
        line = reader.readLine();
      }
    }

    return itemsToFetch;
  }
}
