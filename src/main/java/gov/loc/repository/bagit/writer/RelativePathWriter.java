package gov.loc.repository.bagit.writer;

import java.nio.file.Path;

import gov.loc.repository.bagit.util.PathUtils;

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
