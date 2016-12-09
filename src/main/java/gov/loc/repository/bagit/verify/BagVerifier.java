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
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.exceptions.VerificationException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.reader.BagReader;
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
  
  private final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping;
  
  public BagVerifier(){
    nameMapping = new StandardBagitAlgorithmNameToSupportedAlgorithmMapping();
  }
  
  public BagVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    this.nameMapping = nameMapping;
  }
  
  /**
   * Determine if we can quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the {@link Bag} object you wish to check
   * @return true if the bag can be quickly verified
   */
  public boolean canQuickVerify(final Bag bag){
    final String payloadOxum = getPayloadOxum(bag);
    logger.debug("Found payload-oxum [{}] for bag [{}]", payloadOxum, bag.getRootDir());
    return payloadOxum != null && payloadOxum.matches(PAYLOAD_OXUM_REGEX) && bag.getItemsToFetch().size() == 0;
  }
  
  /*
   * Get the Payload-Oxum value from the key value pairs
   */
  protected String getPayloadOxum(final Bag bag){
    for(final Pair<String,String> keyValue : bag.getMetadata()){
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
  public void quicklyVerify(final Bag bag, final boolean ignoreHiddenFiles) throws IOException, InvalidPayloadOxumException{
    final String payloadOxum = getPayloadOxum(bag);
    if(payloadOxum == null || !payloadOxum.matches(PAYLOAD_OXUM_REGEX)){
      throw new PayloadOxumDoesNotExistException("Payload-Oxum does not exist in bag.");
    }

    final String[] parts = payloadOxum.split("\\.");
    logger.debug("Parsing [{}] for the total byte size of the payload oxum", parts[0]);
    final long totalSize = Long.parseLong(parts[0]);
    logger.debug("Parsing [{}] for the number of files to find in the payload directory", parts[1]);
    final long numberOfFiles = Long.parseLong(parts[1]);
    
    final Path payloadDir = getDataDir(bag);
    final FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor(ignoreHiddenFiles);
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
   * @throws VerificationException some other exception happened during processing so capture it here.
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   */
  public void isValid(final Bag bag, final boolean ignoreHiddenFiles) throws IOException, NoSuchAlgorithmException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, FileNotInPayloadDirectoryException, InterruptedException, MaliciousManifestException, CorruptChecksumException, VerificationException, UnsupportedAlgorithmException{
    logger.info("Checking if the bag with root directory [{}] is valid.", bag.getRootDir());
    isComplete(bag, ignoreHiddenFiles);
    
    logger.debug("Checking payload manifest(s) checksums");
    for(final Manifest payloadManifest : bag.getPayLoadManifests()){
      checkHashes(payloadManifest);
    }
    
    logger.debug("Checking tag manifest(s) checksums");
    for(final Manifest tagManifest : bag.getTagManifests()){
      checkHashes(tagManifest);
    }
  }
  
  /*
   * Check the supplied checksum hashes against the generated checksum hashes
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  protected void checkHashes(final Manifest manifest) throws CorruptChecksumException, InterruptedException, VerificationException{
    final ExecutorService executor = Executors.newCachedThreadPool();
    final CountDownLatch latch = new CountDownLatch( manifest.getFileToChecksumMap().size());
    final List<Exception> exceptions = new ArrayList<>(); //TODO maybe return all of these at some point...
    
    for(final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
      executor.execute(new CheckManifestHashsTask(entry, manifest.getAlgorithm().getMessageDigestName(), latch, exceptions));
    }
    
    latch.await();
    executor.shutdown();
    
    if(!exceptions.isEmpty()){
      final Exception e = exceptions.get(0);
      if(e instanceof CorruptChecksumException){
        logger.debug("[{}] hashes don't match, but I can only return one exception", exceptions.size());
        throw (CorruptChecksumException)e;
      }
      
      throw new VerificationException(e);
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
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   */
  public void isComplete(final Bag bag, final boolean ignoreHiddenFiles) throws 
    IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, 
    FileNotInPayloadDirectoryException, InterruptedException, MaliciousManifestException, UnsupportedAlgorithmException{
    logger.info("Checking if the bag with root directory [{}] is complete.", bag.getRootDir());
    
    final Path dataDir = getDataDir(bag);
    
    checkFetchItemsExist(bag.getItemsToFetch(), bag.getRootDir());
    
    checkBagitFileExists(bag.getRootDir(), bag.getVersion());
    
    checkPayloadDirectoryExists(bag);
    
    checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion());
    
    final Set<Path> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    checkAllFilesListedInManifestExist(allFilesListedInManifests);
    checkAllFilesInPayloadDirAreListedInAManifest(allFilesListedInManifests, dataDir, ignoreHiddenFiles);
  }
  
  /*
   * Get the directory that contains the payload files.
   */
  protected Path getDataDir(final Bag bag){
    if(bag.getVersion().compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      return bag.getRootDir();
    }
    
    return bag.getRootDir().resolve(PAYLOAD_DIR_NAME);
  }
  
  /*
   * make sure all the fetch items exist in the data directory
   */
  protected void checkFetchItemsExist(final List<FetchItem> items, final Path bagDir) throws FileNotInPayloadDirectoryException{
    logger.info("Checking if all [{}] items in fetch.txt exist in the [{}]", items.size(), bagDir);
    for(final FetchItem item : items){
      final Path file = bagDir.resolve(item.path);
      if(!Files.exists(file)){
        throw new FileNotInPayloadDirectoryException("Fetch item " + item + " has not been fetched!");
      }
    }
  }
  
  /*
   * make sure the bagit.txt file exists
   */
  protected void checkBagitFileExists(final Path rootDir, final Version version) throws MissingBagitFileException{
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
  
  /*
   * Make sure the data directory exists
   */
  protected void checkPayloadDirectoryExists(final Bag bag) throws MissingPayloadDirectoryException{
    logger.info("Checking if special payload directory exists (only for version 0.97 and earlier)");
    final Path dataDir = getDataDir(bag);
    
    if(!Files.exists(dataDir)){
      throw new MissingPayloadDirectoryException("File [" + dataDir + "] should exist but it doesn't");
    }
  }
  
  /*
   * Must have at least one manifest-<ALGORITHM>.txt file
   */
  protected void checkIfAtLeastOnePayloadManifestsExist(final Path rootDir, final Version version) throws MissingPayloadManifestException, IOException{
    logger.info("Checking if there is at least one payload manifest in [{}]", rootDir);
    boolean hasAtLeastOneManifest = false;
    
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootDir);
    //@Incubating
    if(version.compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      directoryStream = Files.newDirectoryStream(rootDir.resolve(DOT_BAGIT_DIR_NAME));
    }
    
    for(final Path path : directoryStream){
      if(PathUtils.getFilename(path).startsWith("manifest-")){
        logger.debug("Found payload manifest file [{}]", path.getFileName());
        hasAtLeastOneManifest = true;
      }
    }
    
    if(!hasAtLeastOneManifest){
      throw new MissingPayloadManifestException("Bag does not contain any payload manifest files");
    }
    
  }
  
  /*
   * get all the files listed in all the manifests
   */
  protected Set<Path> getAllFilesListedInManifests(final Bag bag) throws IOException, MaliciousManifestException, UnsupportedAlgorithmException{
    logger.debug("Getting all files listed in the manifest(s)");
    final Set<Path> filesListedInManifests = new HashSet<>();
    
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(bag.getRootDir());
    //@Incubating
    if(bag.getVersion().compareTo(new Version(0, 98)) >= 0){ //is it a .bagit version?
      directoryStream = Files.newDirectoryStream(bag.getRootDir().resolve(DOT_BAGIT_DIR_NAME));
    }
    
    final BagReader reader = new BagReader(nameMapping);
    
    for(final Path path : directoryStream){
      final String filename = PathUtils.getFilename(path);
      if(filename.startsWith("tagmanifest-") || filename.startsWith("manifest-")){
        logger.debug("Getting files and checksums listed in [{}]", path);
        final Manifest manifest = reader.readManifest(path, bag.getRootDir(), bag.getFileEncoding());
        filesListedInManifests.addAll(manifest.getFileToChecksumMap().keySet());
      }
    }
    
    return filesListedInManifests;
  }
  
  /*
   * Make sure all the listed files actually exist
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  protected void checkAllFilesListedInManifestExist(final Set<Path> files) throws FileNotInPayloadDirectoryException, InterruptedException{
    final ExecutorService executor = Executors.newCachedThreadPool();
    final CountDownLatch latch = new CountDownLatch(files.size());
    final List<Path> missingFiles = new ArrayList<>();
    
    logger.debug("Checking if all files listed in the manifest(s) exist");
    for(final Path file : files){
      executor.execute(new CheckIfFileExistsTask(file, missingFiles, latch));
    }
    
    latch.await();
    executor.shutdown();
    
    if(!missingFiles.isEmpty()){
      throw new FileNotInPayloadDirectoryException("Manifest(s) contains file(s) " + missingFiles + " but they don't exist!");
    }
  }
  
  /*
   * Make sure all files in the directory are in at least 1 manifest
   */
  protected void checkAllFilesInPayloadDirAreListedInAManifest(final Set<Path> filesListedInManifests, final Path payloadDir, final boolean ignoreHiddenFiles) throws IOException{
    logger.debug("Checking if all payload files (files in {} dir) are listed in at least one manifest", payloadDir);
    if(Files.exists(payloadDir)){
      Files.walkFileTree(payloadDir, new PayloadFileExistsInManifestVistor(filesListedInManifests, ignoreHiddenFiles));
    }
  }

  public BagitAlgorithmNameToSupportedAlgorithmMapping getNameMapping() {
    return nameMapping;
  }
}
