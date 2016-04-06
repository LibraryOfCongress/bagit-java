package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidPayloadOxumException;
import gov.loc.repository.bagit.exceptions.MaliciousManifestException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.exceptions.PayloadOxumDoesNotExistException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.tasks.CheckIfFileExistsTask;
import gov.loc.repository.bagit.tasks.CheckManifestHashsTask;
import gov.loc.repository.bagit.util.PathUtils;
import javafx.util.Pair;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public class BagVerifier {
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);
  
  private static final String PAYLOAD_DIR_NAME = "data";
  //@Incubating
  private static final String DOT_BAGIT_DIR_NAME = ".bagit";
  private static final String PAYLOAD_OXUM_REGEX = "\\d+\\.\\d+";
  
  private BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping;
  
  public BagVerifier(){
    nameMapping = new StandardBagitAlgorithmNameToSupportedAlgorithmMapping();
  }
  
  public BagVerifier(BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    this.nameMapping = nameMapping;
  }
  
  /**
   * Determine if we can quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the {@link Bag} object you wish to check
   * @return true if the bag can be quickly verified
   */
  public boolean canQuickVerify(Bag bag){
    String payloadOxum = getPayloadOxum(bag);
    logger.debug("Found payload-oxum [{}] for bag [{}]", payloadOxum, bag.getRootDir());
    return payloadOxum != null && payloadOxum.matches(PAYLOAD_OXUM_REGEX) && bag.getItemsToFetch().size() == 0;
  }
  
  protected String getPayloadOxum(Bag bag){
    for(Pair<String,String> keyValue : bag.getMetadata()){
      if("Payload-Oxum".equals(keyValue.getKey())){
        return keyValue.getValue();
      }
    }
    return null;
  }
  
  /**
   * Quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the bag to verify by payload-oxum
   * @param ignoreHiddenFiles ignore hidden files found in payload directory
   * 
   * @throws IOException if there is an error reading a file
   * @throws InvalidPayloadOxumException if either the total bytes or the number of files 
   * calculated for the payload directory of the bag is different than the supplied values
   * @throws PayloadOxumDoesNotExistException if the bag does not contain a payload-oxum.
   * To check, run {@link BagVerifier#canQuickVerify}
   */
  public void quicklyVerify(Bag bag, boolean ignoreHiddenFiles) throws IOException, InvalidPayloadOxumException{
    String payloadOxum = getPayloadOxum(bag);
    if(payloadOxum == null || !payloadOxum.matches(PAYLOAD_OXUM_REGEX)){
      throw new PayloadOxumDoesNotExistException("Payload-Oxum does not exist in bag.");
    }

    String[] parts = payloadOxum.split("\\.");
    logger.debug("Parsing [{}] for the total byte size of the payload oxum", parts[0]);
    long totalSize = Long.parseLong(parts[0]);
    logger.debug("Parsing [{}] for the number of files to find in the payload directory", parts[1]);
    long numberOfFiles = Long.parseLong(parts[1]);
    
    Path payloadDir = getDataDir(bag);
    FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor(ignoreHiddenFiles);
    Files.walkFileTree(payloadDir, vistor);
    logger.info("supplied payload-oxum: [{}], Calculated payload-oxum: [{}.{}], for payload directory [{}]", payloadOxum, vistor.getTotalSize(), vistor.getCount(), payloadDir);
    
    if(totalSize != vistor.getTotalSize()){
      throw new InvalidPayloadOxumException("Invalid total size. Expected " + totalSize + "but calculated " + vistor.getTotalSize());
    }
    if(numberOfFiles != vistor.getCount()){
      throw new InvalidPayloadOxumException("Invalid file count. Expected " + numberOfFiles + "but found " + vistor.getCount() + " files");
    }
  }

  /**
   * See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a><br>
   *  A bag is <b>valid</b> if the bag is complete and every checksum has been 
   *  verified against the contents of its corresponding file.
   * 
   * @param bag the {@link Bag} object to check
   * @param ignoreHiddenFiles ignore hidden files unless explicitly listed in manifest(s)
   * 
   * @throws NoSuchAlgorithmException when trying to generate a {@link MessageDigest} 
   * @throws CorruptChecksumException when the computed hash doesn't match given hash
   * @throws IOException if there was an error with the file
   * @throws MissingPayloadManifestException if there is not at least one payload manifest
   * @throws MissingBagitFileException  if there is no bagit.txt file
   * @throws MissingPayloadDirectoryException if there is no /data directory
   * @throws FileNotInPayloadDirectoryException if a manifest lists a file but it is not in the payload directory
   * @throws InterruptedException if the threads are interrupted when checking if all files are listed in manifest(s)
   * @throws MaliciousManifestException if there is path that is referenced in the manifest that is outside the bag root directory
   */
  public void isValid(Bag bag, boolean ignoreHiddenFiles) throws Exception, IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, FileNotInPayloadDirectoryException, InterruptedException, MaliciousManifestException{
    logger.info("Checking if the bag with root directory [{}] is valid.", bag.getRootDir());
    isComplete(bag, ignoreHiddenFiles);
    
    logger.debug("Checking payload manifest(s) checksums");
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      checkHashes(payloadManifest);
    }
    
    logger.debug("Checking tag manifest(s) checksums");
    for(Manifest tagManifest : bag.getTagManifests()){
      checkHashes(tagManifest);
    }
  }
  
  /**
   * Check the supplied checksum hashes against the generated checksum hashes
   * 
   * @param manifest list of file and their hash
   * 
   * @throws CorruptChecksumException if any of the files computed checksum is different than the manifest supplied checksum
   */
  protected void checkHashes(Manifest manifest) throws Exception{
    ExecutorService executor = Executors.newCachedThreadPool();
    final CountDownLatch latch = new CountDownLatch( manifest.getFileToChecksumMap().size());
    final List<Exception> exceptions = new ArrayList<>(); //TODO maybe return all of these at some point...
    
    for(Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
      executor.execute(new CheckManifestHashsTask(entry, manifest.getAlgorithm().getMessageDigestName(), latch, exceptions));
    }
    
    latch.await();
    
    if(exceptions.size() > 0){
      logger.debug("[{}] hashes don't match, but I can only return one exception", exceptions.size());
      throw exceptions.get(0);
    }
  }
  
  /**
   * See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a><br>
   * A bag is <b>complete</b> if <br>
   * <ul>
   * <li>every element is present
   * <li>every file in the payload manifest(s) are present
   * <li>every file in the tag manifest(s) are present. Tag files not listed in a tag manifest may be present.
   * <li>every file in the data directory must be listed in at least one payload manifest
   * <li>each element must comply with the bagit spec
   * </ul>
   * 
   * @param bag the {@link Bag} object to check
   * @param ignoreHiddenFiles ignore hidden files unless explicitly listed in manifest(s)
   * 
   * @throws IOException if there was an error with the file
   * @throws MissingPayloadManifestException if there is not at least one payload manifest
   * @throws MissingBagitFileException  if there is no bagit.txt file
   * @throws MissingPayloadDirectoryException if there is no /data directory
   * @throws FileNotInPayloadDirectoryException if a manifest lists a file but it is not in the payload directory
   * @throws InterruptedException if the threads are interrupted when checking if all files are listed in manifest(s)
   * @throws MaliciousManifestException if there is path that is referenced in the manifest that is outside the bag root directory
   */
  public void isComplete(Bag bag, boolean ignoreHiddenFiles) throws 
    IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, 
    FileNotInPayloadDirectoryException, InterruptedException, MaliciousManifestException{
    logger.info("Checking if the bag with root directory [{}] is complete.", bag.getRootDir());
    
    Path dataDir = getDataDir(bag);
    
    checkFetchItemsExist(bag.getItemsToFetch(), dataDir);
    
    checkBagitFileExists(bag.getRootDir(), bag.getVersion());
    
    checkPayloadDirectoryExists(bag);
    
    checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion());
    
    Set<Path> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    checkAllFilesListedInManifestExist(allFilesListedInManifests);
    checkAllFilesInPayloadDirAreListedInAManifest(allFilesListedInManifests, dataDir, ignoreHiddenFiles);
  }
  
  protected Path getDataDir(Bag bag){
    if(bag.getVersion().compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      return bag.getRootDir();
    }
    
    return bag.getRootDir().resolve(PAYLOAD_DIR_NAME);
  }
  
  protected void checkFetchItemsExist(List<FetchItem> items, Path dataDir) throws FileNotInPayloadDirectoryException{
    logger.info("Checking if all [{}] items in fetch.txt exist in the [{}]", items.size(), dataDir);
    for(FetchItem item : items){
      Path file = dataDir.resolve(item.path);
      if(!Files.exists(file)){
        throw new FileNotInPayloadDirectoryException("Fetch item " + item + " has not been fetched!");
      }
    }
  }
  
  protected void checkBagitFileExists(Path rootDir, Version version) throws MissingBagitFileException{
    logger.info("Checking if bagit.txt file exists");
    Path bagitFile = rootDir.resolve("bagit.txt");
    //@Incubating
    if(version.compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      bagitFile = rootDir.resolve(DOT_BAGIT_DIR_NAME + File.separator + "bagit.txt");
    }
    
    if(!Files.exists(bagitFile)){
      throw new MissingBagitFileException("File [" + bagitFile + "] should exist but it doesn't");
    }
  }
  
  protected void checkPayloadDirectoryExists(Bag bag) throws MissingPayloadDirectoryException{
    logger.info("Checking if special payload directory exists (only for version 0.97 and earlier)");
    Path dataDir = getDataDir(bag);
    
    if(!Files.exists(dataDir)){
      throw new MissingPayloadDirectoryException("File [" + dataDir + "] should exist but it doesn't");
    }
  }
  
  protected void checkIfAtLeastOnePayloadManifestsExist(Path rootDir, Version version) throws MissingPayloadManifestException, IOException{
    logger.info("Checking if there is at least one payload manifest in [{}]", rootDir);
    boolean hasAtLeastOneManifest = false;
    
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootDir);
    //@Incubating
    if(version.compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      directoryStream = Files.newDirectoryStream(rootDir.resolve(DOT_BAGIT_DIR_NAME));
    }
    
    for(Path path : directoryStream){
      if(PathUtils.getFilename(path).startsWith("manifest-")){
        logger.debug("Found payload manifest file [{}]", path.getFileName());
        hasAtLeastOneManifest = true;
      }
    }
    
    if(!hasAtLeastOneManifest){
      throw new MissingPayloadManifestException("Bag does not contain any payload manifest files");
    }
    
  }
  
  protected Set<Path> getAllFilesListedInManifests(Bag bag) throws IOException, MaliciousManifestException{
    logger.debug("Getting all files listed in the manifest(s)");
    Set<Path> filesListedInManifests = new HashSet<>();
    
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(bag.getRootDir());
    //@Incubating
    if(bag.getVersion().compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      directoryStream = Files.newDirectoryStream(bag.getRootDir().resolve(DOT_BAGIT_DIR_NAME));
    }
    
    BagReader reader = new BagReader(nameMapping);
    
    for(Path path : directoryStream){
      String filename = PathUtils.getFilename(path);
      if(filename.startsWith("tagmanifest-") || filename.startsWith("manifest-")){
        logger.debug("Getting files and checksums listed in [{}]", path);
        Manifest manifest = reader.readManifest(path, bag.getRootDir());
        filesListedInManifests.addAll(manifest.getFileToChecksumMap().keySet());
      }
    }
    
    return filesListedInManifests;
  }
  
  protected void checkAllFilesListedInManifestExist(Set<Path> files) throws FileNotInPayloadDirectoryException, InterruptedException{
    ExecutorService executor = Executors.newCachedThreadPool();
    final CountDownLatch latch = new CountDownLatch(files.size());
    final StringBuilder messageBuilder = new StringBuilder();
    
    logger.debug("Checking if all files listed in the manifest(s) exist");
    for(Path file : files){
      executor.execute(new CheckIfFileExistsTask(file, messageBuilder, latch));
    }
    
    latch.await();
    
    String missingFilesMessage = messageBuilder.toString();
    if(!missingFilesMessage.isEmpty()){
      throw new FileNotInPayloadDirectoryException(missingFilesMessage);
    }
  }
  
  protected void checkAllFilesInPayloadDirAreListedInAManifest(Set<Path> filesListedInManifests, Path payloadDir, boolean ignoreHiddenFiles) throws IOException{
    logger.debug("Checking if all payload files (files in {} dir) are listed in at least one manifest", payloadDir);
    if(Files.exists(payloadDir)){
      Files.walkFileTree(payloadDir, new PayloadFileExistsInManifestVistor(filesListedInManifests, ignoreHiddenFiles));
    }
  }
}
