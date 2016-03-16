package gov.loc.repository.bagit.operations;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.annotation.Incubating;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.verify.BagVerifier;
import javafx.util.Pair;

/**
 * Responsible for creating and manipulating bags.
 */
public class BagManipulator {
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);

  private BagReader bagReader;
  
  public BagManipulator(){
    bagReader = new BagReader();
  }
  
  /**
   * Use this constructor if you are using a custom checksum algorithm
   * 
   * @param nameMapping the modified name mapping for the custom checksum algorithm
   */
  public BagManipulator(BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    bagReader = new BagReader(nameMapping); 
  }
  
  /**
   * updates or inserts the metadata key value pair in the bag. 
   * If there are multiple of the same key (which is allowed) it only updates the first occurrence. 
   * Also updates any tag manifests that reference the metadata file (bag-info.txt)
   * This method updates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param metaDataToUpsert the metadata to update or insert
   * @param root the directory that is the base of the bag
   * @return the bag with the updated metadata
   * @throws IOException if there is a problem writing files
   * @throws UnparsableVersionException If there is a problem parsing the bagit version
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   */
  @Incubating
  public Bag upsertMetadata(Pair<String, String> metaDataToUpsert, Path root) throws IOException, UnparsableVersionException, NoSuchAlgorithmException{
    Bag bag = bagReader.read(root);
    boolean updated = updateMetadata(bag, metaDataToUpsert);
    
    if(!updated){ //the metadata didn't exist, so just add it
      bag.getMetadata().add(metaDataToUpsert);
    }
    
    BagWriter.writeBagitInfoFile(bag.getMetadata(), bag.getRootDir(), bag.getFileEncoding());
    
    Path bagInfoPath = bag.getRootDir().resolve("bag-info.txt");
    if(bag.getVersion().compareTo(new Version(0, 96)) < 0){ //if it is a version before 0.96
      bagInfoPath = bag.getRootDir().resolve("package-info.txt");  
    }
    updateTagManifests(bag, bagInfoPath);
    
    return bag;
  }
  
  protected boolean updateMetadata(Bag bag, Pair<String, String> metaDataToUpsert){
    for(int index=0; index < bag.getMetadata().size(); index++){
      Pair<String, String> metadata = bag.getMetadata().get(index);
      if(metaDataToUpsert.getKey().equals(metadata.getKey())){
        bag.getMetadata().remove(index);
        bag.getMetadata().add(index, metaDataToUpsert);
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Remove a file from all manifests in a bag. It does not delete it.
   * This method updates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param fileToRemove the file you wish to remove from all manifests that contain it
   * @param bag the existing bag you wish to update
   * @return the updated bag
   * @throws IOException if there is a problem writing files
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   */
  @Incubating
  public Bag removeFileFromPayloadManifests(Path fileToRemove, Bag bag) throws IOException, NoSuchAlgorithmException{
    
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      payloadManifest.getFileToChecksumMap().remove(fileToRemove);
      BagWriter.writeManifest(payloadManifest, bag.getRootDir(), "manifest", bag.getFileEncoding());
      updateTagManifests(bag, bag.getRootDir().resolve("manifest-" + payloadManifest.getAlgorithm().getBagitName() + ".txt"));
    }
    
    return bag;
  }
  
  /**
   * Adds multiple files to the payload manifest of an existing bag. 
   * The files should already be located inside the bag payload directory. 
   * This method updates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param filesToAdd the files you wish to add to the payload manifest
   * @param bag the existing bag you wish to update
   * @param algorithm an implementation of {@link SupportedAlgorithm}
   * @return the updated bag
   * @throws IOException if there is a problem writing files
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   */
  @Incubating
  public Bag addFilesToPayloadManifest(Collection<Path> filesToAdd, Bag bag, SupportedAlgorithm algorithm) throws NoSuchAlgorithmException, IOException{
    Manifest payloadManifest = getPayloadManifest(bag, algorithm);
    
    for(Path fileToAdd : filesToAdd){
      MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
      String checksum = Hasher.hash(Files.newInputStream(fileToAdd), messageDigest);
      payloadManifest.getFileToChecksumMap().put(fileToAdd, checksum);
    }
    bag.getPayLoadManifests().add(payloadManifest);
    
    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), bag.getRootDir(), bag.getFileEncoding());
    
    Path payloadManifestPath = bag.getRootDir().resolve("manifest-" + algorithm.getBagitName() + ".txt");
    updateTagManifests(bag, payloadManifestPath);
    
    return bag;
  }
  
  /**
   * Adds a file to the payload manifest of an existing bag. 
   * The file should already be located inside the bag payload directory.
   * This method updates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param fileToAdd the file that you wish to add to the manifest
   * @param bag the existing bag you wish to update
   * @param algorithm an implementation of {@link SupportedAlgorithm}
   * @return the updated bag
   * @throws IOException if there is a problem writing files
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   */
  @Incubating
  public Bag addFileToPayloadManifest(Path fileToAdd, Bag bag, SupportedAlgorithm algorithm) throws IOException, NoSuchAlgorithmException{
    Manifest payloadManifest = getPayloadManifest(bag, algorithm);
    
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    String checksum = Hasher.hash(Files.newInputStream(fileToAdd), messageDigest);
    payloadManifest.getFileToChecksumMap().put(fileToAdd, checksum);
    bag.getPayLoadManifests().add(payloadManifest);

    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), bag.getRootDir(), bag.getFileEncoding());
    
    Path payloadManifestPath = bag.getRootDir().resolve("manifest-" + algorithm.getBagitName() + ".txt");
    updateTagManifests(bag, payloadManifestPath);
    
    return bag;
  }
  
  protected Manifest getPayloadManifest(Bag bag, SupportedAlgorithm algorithm){
    for(Manifest manifest : bag.getPayLoadManifests()){
      if(algorithm.equals(manifest.getAlgorithm())){
        return manifest;
      }
    }
    
    return new Manifest(algorithm);
  }
  
  protected void updateTagManifests(Bag bag, Path pathToUpdate) throws NoSuchAlgorithmException, IOException{
    for(Manifest tagManifest : bag.getTagManifests()){
      MessageDigest messageDigest = MessageDigest.getInstance(tagManifest.getAlgorithm().getMessageDigestName());
      String checksum = Hasher.hash(Files.newInputStream(pathToUpdate), messageDigest);
      if(tagManifest.getFileToChecksumMap().get(pathToUpdate) != null){
        tagManifest.getFileToChecksumMap().put(pathToUpdate, checksum);
      }
    }
    BagWriter.writeTagManifests(bag.getTagManifests(), bag.getRootDir(), bag.getFileEncoding());
  }
  
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
  public Bag bagInPlace(Path root, SupportedAlgorithm algorithm, boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    Bag bag = new Bag(new Version(0, 97));
    bag.setRootDir(root);
    logger.info("Creating a bag with version: [{}] in directory: [{}]", bag.getVersion(), root);
    
    
    Path dataDir = root.resolve("data");
    Files.createDirectory(dataDir);
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root);
    for(Path path : directoryStream){
      if(!path.equals(dataDir) && !Files.isHidden(path) || includeHidden){
        Files.move(path, dataDir.resolve(path.getFileName()));
      }
    }
    
    logger.info("Creating payload manifest");
    Manifest manifest = new Manifest(algorithm);
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    AddPayloadToBagManifestVistor visitor = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHidden);
    Files.walkFileTree(dataDir, visitor);
    
    bag.getPayLoadManifests().add(manifest);
    BagWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), root);
    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), root, bag.getFileEncoding());
    
    
    return bag;
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
  public Bag createDotBagit(Path root, SupportedAlgorithm algorithm, boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    Bag bag = new Bag(new Version(0, 98));
    bag.setRootDir(root);
    logger.info("Creating a bag with version: [{}] in directory: [{}]", bag.getVersion(), root);
    
    Path dotbagitDir = root.resolve(".bagit");
    Files.createDirectories(dotbagitDir);
    
    logger.info("Creating payload manifest");
    Manifest manifest = new Manifest(algorithm);
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    AddPayloadToBagManifestVistor visitor = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHidden);
    Files.walkFileTree(root, visitor);
    
    bag.getPayLoadManifests().add(manifest);
    BagWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), dotbagitDir);
    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), dotbagitDir, bag.getFileEncoding());
    
    return bag;
  }
}