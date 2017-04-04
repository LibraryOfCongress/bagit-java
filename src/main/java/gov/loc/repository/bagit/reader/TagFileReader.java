package gov.loc.repository.bagit.reader;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.util.PathUtils;

public interface TagFileReader {
  Logger logger = LoggerFactory.getLogger(TagFileReader.class);
  String ERROR_PREFIX = "Path [";
  
  /*
   * Create the file and check it for various things, like starting with a *, or trying to access a file outside the bag
   */
  static Path createFileFromManifest(final Path bagRootDir, final String path) throws MaliciousPathException, InvalidBagitFileFormatException{
    String fixedPath = path;
    if(path.charAt(0) == '*'){
      logger.warn("Encountered path that was created by non-bagit tool. Removing * from path. Please remove all * from manifest files!");
      fixedPath = path.substring(1); //remove the * from the path
    }
    
    if(path.contains("\\")){
      throw new InvalidBagitFileFormatException(ERROR_PREFIX + path + "] is invalid due to the use of the path separactor [\\]");
    }
    
    if(path.contains("~/")){
      throw new MaliciousPathException(ERROR_PREFIX + path + "] is trying to be malicious and access a file outside the bag");
    }

    fixedPath = PathUtils.decodeFilname(fixedPath);
    Path file;
    if(fixedPath.startsWith("file://")){
      try {
        file = Paths.get(new URI(fixedPath));
      } catch (URISyntaxException e) {
        throw new InvalidBagitFileFormatException("URL [" + path + "] is invalid.", e);
      }
    }
    else{
      file = bagRootDir.resolve(fixedPath).normalize();
    }
    
    if(!file.normalize().startsWith(bagRootDir)){
      throw new MaliciousPathException(ERROR_PREFIX + file + "] is outside the bag root directory of " + bagRootDir + 
          "! This is not allowed according to the bagit specification!");
    }
    
    return file;
  }
}
