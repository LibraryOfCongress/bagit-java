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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import nl.knaw.dans.bagit.util.PathUtils;

/**
 * Convenience class for reading tag files from the filesystem
 */
public interface TagFileReader {
  Logger logger = LoggerFactory.getLogger(TagFileReader.class);
  ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  String ERROR_PREFIX = "Path [";
  
  /*
   * Create the file and check it for various things, like starting with a *, or trying to access a file outside the bag
   */
  static Path createFileFromManifest(final Path bagRootDir, final String path) throws MaliciousPathException, InvalidBagitFileFormatException {
    String fixedPath = path;
    if(path.charAt(0) == '*'){
      logger.warn(messages.getString("removing_asterisk"));
      fixedPath = path.substring(1); //remove the * from the path
    }
    
    if(path.contains("\\")){
      final String formattedMessage = messages.getString("blackslash_used_as_path_separator_error");
      throw new InvalidBagitFileFormatException(MessageFormatter.format(formattedMessage, path).getMessage());
    }
    
    if(path.contains("~/")){
      final String formattedMessage = messages.getString("malicious_path_error");
      throw new MaliciousPathException(MessageFormatter.format(formattedMessage, path).getMessage());
    }

    fixedPath = PathUtils.decodeFilname(fixedPath);
    Path file;
    if(fixedPath.startsWith("file://")){
      try {
        file = Paths.get(new URI(fixedPath));
      } catch (URISyntaxException e) {
        final String formattedMessage = messages.getString("invalid_url_format_error");
        throw new InvalidBagitFileFormatException(MessageFormatter.format(formattedMessage, path).getMessage(), e);
      }
    }
    else{
      file = bagRootDir.resolve(fixedPath).normalize();
    }
    
    if(!file.normalize().startsWith(bagRootDir)){
      final String formattedMessage = messages.getString("malicious_path_error");
      throw new MaliciousPathException(MessageFormatter.format(formattedMessage, file).getMessage());
    }
    
    return file;
  }
}
