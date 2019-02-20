package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * Responsible for checking all things related to mandatory files for the bagit specification
 */
public final class MandatoryVerifier {
  private static final Logger logger = LoggerFactory.getLogger(MandatoryVerifier.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  //@Incubating
  private static final String DOT_BAGIT_DIR_NAME = ".bagit";
  
  private MandatoryVerifier(){
    //intentionally left blank
  }

  /**
   * make sure all the fetch items exist in the data directory
   * 
   * @param items the items that needed to be fetched for the bag to be complete
   * @param bagDir the root directory of the bag
   * @throws FileNotInPayloadDirectoryException if one or more of the fetch items don't exist
   */
  public static void checkFetchItemsExist(final List<FetchItem> items, final Path bagDir) throws FileNotInPayloadDirectoryException{
    logger.info(messages.getString("checking_fetch_items_exist"), items.size(), bagDir);
    for(final FetchItem item : items){
      if(!Files.exists(item.path)){
        final String formattedMessage = messages.getString("fetch_item_missing_error");
        throw new FileNotInPayloadDirectoryException(MessageFormatter.format(formattedMessage, item).getMessage());
      }
    }
  }
  
  /**
   * make sure the bagit.txt file exists
   * 
   * @param rootDir the root directory of the bag
   * @param version the version of the bag
   * @throws MissingBagitFileException if the bag does not contain the bagit.txt file as required by the bagit specification
   */
  public static void checkBagitFileExists(final Path rootDir, final Version version) throws MissingBagitFileException{
    logger.info("Checking if bagit.txt file exists");
    Path bagitFile = rootDir.resolve("bagit.txt");
    //@Incubating
    if(version.isSameOrNewer(new Version(2, 0))){ //is it a .bagit version?
      bagitFile = rootDir.resolve(DOT_BAGIT_DIR_NAME + File.separator + "bagit.txt");
    }
    
    if(!Files.exists(bagitFile)){
      final String formattedMessage = messages.getString("file_should_exist_error");
      throw new MissingBagitFileException(MessageFormatter.format(formattedMessage, bagitFile).getMessage());
    }
  }
  
  /**
   * Make sure the payload directory exists
   * 
   * @param bag the bag to check
   * @throws MissingPayloadDirectoryException if the bag does not contain the payload directory
   */
  public static void checkPayloadDirectoryExists(final Bag bag) throws MissingPayloadDirectoryException{
    logger.info(messages.getString("checking_payload_directory_exists"));
    final Path dataDir = PathUtils.getDataDir(bag);
    
    if(!Files.exists(dataDir)){
      throw new MissingPayloadDirectoryException(messages.getString("file_should_exist_error"), dataDir);
    }
  }
  
  /*
   * Must have at least one manifest-<ALGORITHM>.txt file
   */
  /**
   * Check to make sure the bag has at least one payload manifest
   * (manifest-[ALGORITHM].txt)
   * 
   * @param rootDir the root directory of the bag
   * @param version the version of the bag
   * @throws MissingPayloadManifestException if there are no payload manifests in the bag
   * @throws IOException if there was an error reading a file
   */
  public static void checkIfAtLeastOnePayloadManifestsExist(final Path rootDir, final Version version) throws MissingPayloadManifestException, IOException{
    logger.info("Checking if there is at least one payload manifest in [{}]", rootDir);
    boolean hasAtLeastOneManifest = false;
    
    try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(PathUtils.getBagitDir(version, rootDir))){
      for(final Path path : directoryStream){
        if(PathUtils.getFilename(path).startsWith("manifest-")){
          logger.debug(messages.getString("found_payload_manifest"), path.getFileName());
          hasAtLeastOneManifest = true;
        }
      }
    }    
    
    if(!hasAtLeastOneManifest){
      throw new MissingPayloadManifestException(messages.getString("missing_payload_manifest_error"));
    }
    
  }
}
