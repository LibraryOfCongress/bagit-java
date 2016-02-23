package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import gov.loc.repository.bagit.domain.SupportedAlgorithm;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.tasks.CheckIfFileExistsTask;
import gov.loc.repository.bagit.tasks.CheckManifestHashsTask;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public class Verifier {
  private static final Logger logger = LoggerFactory.getLogger(Verifier.class);
  
  private static final String PAYLOAD_DIR_NAME = "data";

  /**
   * See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a><br>
   *  A bag is <b>valid</b> if the bag is complete and every checksum has been 
   *  verified against the contents of its corresponding file.
   * @param bag the {@link Bag} object to check
   * @param algorithm the {@link SupportedAlgorithm} implementation to use to generate checksum hashes
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
   */
  public static void isValid(Bag bag, SupportedAlgorithm algorithm, boolean ignoreHiddenFiles) throws Exception{
    logger.info("Checking if the bag with root directory [{}] is valid.", bag.getRootDir());
    isComplete(bag, ignoreHiddenFiles);
    
    logger.debug("Checking payload manifest(s) checksums");
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      checkHashes(payloadManifest, algorithm);
    }
    
    logger.debug("Checking tag manifest(s) checksums");
    for(Manifest tagManifest : bag.getTagManifests()){
      checkHashes(tagManifest, algorithm);
    }
  }
  
  /**
   * Check the supplied checksum hashes against the generated checksum hashes
   * 
   * @param manifest list of file and their hash
   * @param algorithm the algorithm to use to generate checksum hash
   * @throws CorruptChecksumException if any of the files computed checksum is different than the manifest supplied checksum
   */
  protected static void checkHashes(Manifest manifest, SupportedAlgorithm algorithm) throws Exception{
    logger.debug("Checking manifest using algorithm {}", algorithm.getMessageDigestName());
    
    ExecutorService executor = Executors.newCachedThreadPool();
    final CountDownLatch latch = new CountDownLatch( manifest.getFileToChecksumMap().size());
    final List<Exception> exceptions = new ArrayList<>(); //TODO maybe return all of these at some point...
    
    for(Entry<File, String> entry : manifest.getFileToChecksumMap().entrySet()){
      executor.execute(new CheckManifestHashsTask(entry, algorithm.getMessageDigestName(), latch, exceptions));
    }
    
    latch.await();
    
    if(exceptions.size() > 0){
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
   * @param bag the {@link Bag} object to check
   * @param ignoreHiddenFiles ignore hidden files unless explicitly listed in manifest(s)
   * @throws IOException if there was an error with the file
   * @throws MissingPayloadManifestException if there is not at least one payload manifest
   * @throws MissingBagitFileException  if there is no bagit.txt file
   * @throws MissingPayloadDirectoryException if there is no /data directory
   * @throws FileNotInPayloadDirectoryException if a manifest lists a file but it is not in the payload directory
   * @throws InterruptedException if the threads are interrupted when checking if all files are listed in manifest(s)
   */
  public static void isComplete(Bag bag, boolean ignoreHiddenFiles) throws 
    IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, 
    FileNotInPayloadDirectoryException, InterruptedException{
    logger.info("Checking if the bag with root directory [{}] is complete.", bag.getRootDir());
    
    File dataDir = new File(bag.getRootDir(), PAYLOAD_DIR_NAME);
    checkFetchItemsExist(bag.getItemsToFetch(), dataDir);
    
    checkBagitFileExists(bag.getRootDir());
    
    checkPayloadDirectoryExists(bag.getRootDir());
    
    checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir());
    
    Set<File> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    checkAllFilesListedInManifestExist(allFilesListedInManifests);
    checkAllFilesInPayloadDirAreListedInAManifest(allFilesListedInManifests, bag.getRootDir(), ignoreHiddenFiles);
  }
  
  protected static void checkFetchItemsExist(List<FetchItem> items, File dataDir) throws FileNotInPayloadDirectoryException{
    for(FetchItem item : items){
      File file = new File(dataDir, item.path);
      if(!file.exists()){
        throw new FileNotInPayloadDirectoryException("Fetch item " + item + " has not been fetched!");
      }
    }
  }
  
  protected static void checkBagitFileExists(File rootDir) throws MissingBagitFileException{
    File bagitFile = new File(rootDir, "bagit.txt");
    if(!bagitFile.exists()){
      throw new MissingBagitFileException("File [" + bagitFile + "] should exist but it doesn't");
    }
  }
  
  protected static void checkPayloadDirectoryExists(File rootDir) throws MissingPayloadDirectoryException{
    File dataDir = new File(rootDir, PAYLOAD_DIR_NAME);
    if(!dataDir.exists()){
      throw new MissingPayloadDirectoryException("File [" + dataDir + "] should exist but it doesn't");
    }
  }
  
  protected static void checkIfAtLeastOnePayloadManifestsExist(File rootDir) throws MissingPayloadManifestException{
    boolean hasAtLeastOneManifest = false;
    String[] filenames = rootDir.list();
    if(filenames != null){
      for(String filename : filenames){
        if(filename.matches("manifest\\-.*\\.txt")){
          logger.debug("Found payload manifest file [{}]", filename);
          hasAtLeastOneManifest = true;
        }
      }
    }
    
    if(!hasAtLeastOneManifest){
      throw new MissingPayloadManifestException("Bag does not contain any payload manifest files");
    }
    
  }
  
  protected static Set<File> getAllFilesListedInManifests(Bag bag) throws IOException{
    Set<File> filesListedInManifests = new HashSet<>();
    
    File[] files = bag.getRootDir().listFiles();
    if(files != null){
      for(File file : files){
        if(file.getName().matches("(tag)?manifest\\-.*\\.txt")){
          logger.debug("Getting files and checksums listed in [{}]", file);
          Manifest manifest = BagReader.readManifest(file);
          filesListedInManifests.addAll(manifest.getFileToChecksumMap().keySet());
        }
      }
    }
    
    return filesListedInManifests;
  }
  
  protected static void checkAllFilesListedInManifestExist(Set<File> files) throws FileNotInPayloadDirectoryException, InterruptedException{
    ExecutorService executor = Executors.newCachedThreadPool();
    final CountDownLatch latch = new CountDownLatch(files.size());
    final StringBuilder messageBuilder = new StringBuilder();
    
    logger.debug("Checking if all files listed in the manifest(s) exist");
    for(File file : files){
      executor.execute(new CheckIfFileExistsTask(file, messageBuilder, latch));
    }
    
    latch.await();
    
    String missingFilesMessage = messageBuilder.toString();
    if(!missingFilesMessage.isEmpty()){
      throw new FileNotInPayloadDirectoryException(missingFilesMessage);
    }
  }
  
  protected static void checkAllFilesInPayloadDirAreListedInAManifest(Set<File> filesListedInManifests, File rootDir, boolean ignoreHiddenFiles) throws IOException{
    File payloadDir = new File(rootDir, PAYLOAD_DIR_NAME);
    logger.debug("Checking if all payload files (files in /data dir) are listed in at least one manifest");
    if(payloadDir.exists()){
      Path start = Paths.get(payloadDir.toURI());
      Files.walkFileTree(start, new PayloadFileExistsInManifestVistor(filesListedInManifests, ignoreHiddenFiles));
    }
  }
}
