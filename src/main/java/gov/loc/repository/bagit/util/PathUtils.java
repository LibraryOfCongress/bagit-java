package gov.loc.repository.bagit.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

public final class PathUtils {
	
  private static final boolean isWindows = System.getProperty("os.name").contains("Windows");
  
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
    return PathUtils.encodeFilenameString(path.toString());
  }
  /**
   * Encode a string with percent notation
   * (NOTE:  both this method an decodeFilename should be expanded to handle all pct-encoded characters, and the
   * percent-encoding of all unreserved characters, as per RFC 3986 (https://tools.ietf.org/html/rfc3986)
   * @param path the String to be encoded
   * @return the encoded String
   */
  public static String encodeFilenameString(final String path){	  
	  return path.replaceAll("\n", "%0A").replaceAll("\r", "%0D");
  }
  /**
   * Determine if we are running on Windows OS
   * @return true if running on Windows OS, else false
   */
  public static boolean isWindows() {
	  return PathUtils.isWindows;
  }
  /**
   * Determine if Path represents a hidden file on a Windows FileSystem
   * @param dir Path to be tested
   * @return true if we are running on Windows, and Path represents hidden file
   * @throws IOException
   */
  public static boolean isHiddenWindowsFile(final Path dir) throws IOException{
	  return (PathUtils.isWindows() && 
		        Files.readAttributes(dir, DosFileAttributes.class).isHidden());
  }

}
