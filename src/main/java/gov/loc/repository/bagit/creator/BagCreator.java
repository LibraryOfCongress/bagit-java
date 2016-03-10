package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.annotation.Incubating;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.verify.BagVerifier;
import gov.loc.repository.bagit.writer.BagWriter;

/**
 * Responsible for creating a bag in place.
 */
public class BagCreator {
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);
  
  /**
   * Creates a basic(only required elements) bag in place for version 0.97.
   * This method moves and creates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithm an implementation of {@link SupportedAlgorithm}
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing or moving file(s)
   * @return a {@link Bag} object representing the newly created bagit bag
   */
  public static Bag bagInPlace(File root, SupportedAlgorithm algorithm, boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    Bag bag = new Bag(new Version(0, 97));
    bag.setRootDir(root);
    logger.info("Creating a bag with version: [{}] in directory: [{}]", bag.getVersion(), root);
    
    File[] files = root.listFiles();
    File dataDir = new File(root, "data");
    if(!dataDir.exists() && !dataDir.mkdir()){
      throw new IOException("Unable to make " + dataDir);
    }
    
    moveFilesToDataDir(files, dataDir);
    
    logger.info("Creating payload manifest");
    Manifest manifest = new Manifest(algorithm);
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    AddPayloadToBagManifestVistor visitor = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHidden);
    Files.walkFileTree(Paths.get(dataDir.toURI()), visitor);
    
    bag.getPayLoadManifests().add(manifest);
    BagWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), root);
    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), root, bag.getFileEncoding());
    
    
    return bag;
  }
  
  protected static void moveFilesToDataDir(File[] files, File dataDir) throws IOException{
    if(files != null){
      logger.info("Moving files to [{}]", dataDir);
      for(File file : files){
        Path dest = Paths.get(dataDir.getPath(), file.getName());
        logger.debug("Moving [{}] to [{}]", file, dest);
        Files.move(Paths.get(file.toURI()), dest);
      }
    }
  }
  
  /**
   * Creates a basic(only required elements) .bagit bag in place.
   * This creates files and directories, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithm an implementation of {@link SupportedAlgorithm}
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * @return a {@link Bag} object representing the newly created bagit bag
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing files or .bagit directory
   */
  @Incubating
  public static Bag createDotBagit(File root, SupportedAlgorithm algorithm, boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    Bag bag = new Bag(new Version(0, 98));
    bag.setRootDir(root);
    logger.info("Creating a bag with version: [{}] in directory: [{}]", bag.getVersion(), root);
    
    File dotbagitDir = new File(root, ".bagit");
    if(!dotbagitDir.mkdir()){
      throw new IOException("Was unable to create " + dotbagitDir);
    }
    
    logger.info("Creating payload manifest");
    Manifest manifest = new Manifest(algorithm);
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    AddPayloadToBagManifestVistor visitor = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHidden);
    Files.walkFileTree(Paths.get(root.toURI()), visitor);
    
    bag.getPayLoadManifests().add(manifest);
    BagWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), dotbagitDir);
    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), dotbagitDir, bag.getFileEncoding());
    
    return bag;
  }
}