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
package nl.knaw.dans.bagit.writer;

import java.nio.file.Path;

import nl.knaw.dans.bagit.util.PathUtils;

/**
 * Convenience class for writing a relative path
 */
public final class RelativePathWriter {
  
  private RelativePathWriter(){
    //intentionally left empty
  }
  
  /**
   * Create a relative path that has \ (windows) path separator replaced with / and encodes newlines
   * 
   * @param relativeTo the path to remove from the entry
   * @param entry the path to make relative
   * 
   * @return the relative path with only unix path separator
   */
  public static String formatRelativePathString(final Path relativeTo, final Path entry){
    final String encodedPath = PathUtils.encodeFilename(relativeTo.toAbsolutePath().relativize(entry.toAbsolutePath()));
    return encodedPath.replace('\\', '/') + System.lineSeparator();
  }
}
