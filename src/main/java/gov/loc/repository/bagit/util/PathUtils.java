package gov.loc.repository.bagit.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Version;

public final class PathUtils {
  private static final String PAYLOAD_DIR_NAME = "data";
  
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
  
  /*
   * Get the directory that contains the payload files.
   */
  /**
   * With bagit version 2.0 (.bagit)
   * payload files are no longer in the "data" directory. This method accounts for this
   * and will return the directory that contains the payload files
   * 
   * @param bag that contains the payload files you want
   * @return the directory that contains the payload files 
   */
  public static Path getDataDir(final Bag bag){
    if(bag.getVersion().compareTo(new Version(2, 0)) >= 0){ //is it a .bagit version?
      return bag.getRootDir();
    }
    
    return bag.getRootDir().resolve(PAYLOAD_DIR_NAME);
  }
}
