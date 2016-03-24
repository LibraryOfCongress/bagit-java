package gov.loc.repository.bagit.util;

import java.nio.file.Path;

public class PathUtils {

  /**
   * Needed to get rid of findbugs "dodgy code warnings" in regards to getting the filename of a path as a string
   * 
   * @param path the path that you which to get the filename as a string
   * @return the filename or an empty string
   */
  public static String getFilename(Path path){
    String filename = "";
    if(path != null){
      Path filenamePath = path.getFileName();
      if(filenamePath != null){
        filename = filenamePath.toString();
      }
    }
    
    return filename;
  }
}
