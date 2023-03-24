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
package nl.knaw.dans.bagit.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Version;
import nl.knaw.dans.bagit.verify.FileCountAndTotalSizeVistor;

/**
 * Convenience class for dealing with various path issues
 */
public final class PathUtils {
  private static final String PAYLOAD_DIR_NAME = "data";
  
  //@Incubating
 private static final String DOT_BAGIT_DIR_NAME = ".bagit";
  
  private PathUtils(){
    //intentionally left blank
  }

  /**
   * Needed to get rid of findbugs "dodgy code warnings" in regards to getting the filename of a path as a string
   * 
   * @param path the path that you which to get the filename as a string
   * @return the filename or an empty string
   */
  public static String getFilename(final Path path){
    String filename = "";
    if(path != null){
      final Path filenamePath = path.getFileName();
      if(filenamePath != null){
        filename = filenamePath.toString();
      }
    }
    
    return filename;
  }
  
  /**
   * as per https://github.com/jkunze/bagitspec/commit/152d42f6298b31a4916ea3f8f644ca4490494070 decode percent encoded filenames
   * @param encoded the encoded filename
   * @return the decoded filename 
   */
  public static String decodeFilname(final String encoded){
    return encoded.replaceAll("%0A", "\n").replaceAll("%0D", "\r");
  }
  
  /**
   * as per https://github.com/jkunze/bagitspec/commit/152d42f6298b31a4916ea3f8f644ca4490494070 encode any new lines or carriage returns
   * @param path the path to encode
   * @return the encoded filename
   */
  public static String encodeFilename(final Path path){
    return path.toString().replaceAll("\n", "%0A").replaceAll("\r", "%0D");
  }
  
  /**
   * Due to the way that windows handles hidden files vs. *nix 
   * we use this method to determine if a file or folder is really hidden
   * @param path the file or folder to check if hidden
   * @return if the file or folder is hidden
   * @throws IOException if there is an error reading the file/folder
   */
  public static boolean isHidden(final Path path) throws IOException{
    //cause Files.isHidden() doesn't work properly for windows if the file is a directory
    if (System.getProperty("os.name").contains("Windows")){
      return Files.readAttributes(path, DosFileAttributes.class).isHidden();
    }

    return Files.isHidden(path);
  }
  
  /**
   * With bagit version 2.0 (.bagit)
   * payload files are no longer in the "data" directory. This method accounts for this
   * and will return the directory that contains the payload files
   * 
   * @param bag that contains the payload files you want
   * @return the directory that contains the payload files 
   */
  public static Path getDataDir(final Bag bag){
    if(bag.getVersion().isSameOrNewer(new Version(2, 0))){ //is it a .bagit version?
      return bag.getRootDir();
    }
    
    return bag.getRootDir().resolve(PAYLOAD_DIR_NAME);
  }
  
  /**
   * With bagit version 2.0 (.bagit)
   * payload files are no longer in the "data" directory. This method accounts for this
   * and will return the directory that contains the payload files
   * 
   * @param version the bag version
   * @param output where the bag is being or was written to
   * 
   * @return the payload directory for the output directory
   */
  public static Path getDataDir(final Version version, final Path output){
    if(version.isSameOrNewer(new Version(2, 0))){ //is it a .bagit version?
      return output;
    }
    
    return output.resolve(PAYLOAD_DIR_NAME);
  }
  
  /**
   * With bagit version 2.0 (.bagit) bagit specific files are no longer at the bag root directory.
   * This method accounts for this and will return the directory that contains the bag specific files.
   * 
   * @param bag the bag
   * 
   * @return the directory which contains the bag specific files, like manifests or bagit.txt
   */
  public static Path getBagitDir(final Bag bag){
    if(bag.getVersion().isSameOrNewer(new Version(2, 0))){ //is it a .bagit version?
      return bag.getRootDir().resolve(DOT_BAGIT_DIR_NAME);
    }
    
    return bag.getRootDir();
  }
  
  /**
   * With bagit version 2.0 (.bagit) bagit specific files are no longer at the bag root directory.
   * This method accounts for this and will return the directory that contains the bag specific files.
   * 
   * @param version the bag version
   * @param bagRoot the root directory of the bag
   * 
   * @return the directory which contains the bag specific files, like manifests or bagit.txt
   */
  public static Path getBagitDir(final Version version, final Path bagRoot){
    if(version.isSameOrNewer(new Version(2, 0))){ //is it a .bagit version?
      return bagRoot.resolve(DOT_BAGIT_DIR_NAME);
    }
    
    return bagRoot;
  }
  
  /**
   * Calculate the total file and byte count of the files in the payload directory
   * 
   * @param dataDir the directory to calculate the payload-oxum
   * 
   * @return the string representation of the payload-oxum value
   * 
   * @throws IOException if there is an error reading any of the files
   */
  public static String generatePayloadOxum(final Path dataDir) throws IOException{
    final FileCountAndTotalSizeVistor visitor = new FileCountAndTotalSizeVistor();
    
    Files.walkFileTree(dataDir, visitor);
    
    return visitor.getTotalSize() + "." + visitor.getCount();
  }
}
