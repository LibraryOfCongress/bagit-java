package gov.loc.repository.bagit.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.InvalidPayloadOxumException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.exceptions.PayloadOxumDoesNotExistException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.exceptions.VerificationException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public final class BagVerifier {
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);
  
  private final PayloadVerifier manifestVerifier;
  private final ExecutorService executor;
  
  public BagVerifier(){
    this(Executors.newCachedThreadPool(), new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  public BagVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    this(Executors.newCachedThreadPool(), nameMapping);
  }
  
  public BagVerifier(final ExecutorService executor){
    this(executor, new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  public BagVerifier(final ExecutorService executor, final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    manifestVerifier = new PayloadVerifier(nameMapping);
    this.executor = executor;
  }
  
  /**
   * Determine if we can quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the {@link Bag} object you wish to check
   * @return true if the bag can be quickly verified
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public boolean canQuickVerify(final Bag bag) throws UnparsableVersionException, IOException, InvalidBagMetadataException{
    return QuickVerifier.canQuickVerify(bag);
  }
  
  /**
   * Quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the bag to verify by payload-oxum
   * @param ignoreHiddenFiles ignore hidden files found in payload directory
   * 
   * @throws InvalidPayloadOxumException if either the total bytes or the number of files 
   * calculated for the payload directory of the bag is different than the supplied values
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   * @throws PayloadOxumDoesNotExistException if the bag does not contain a payload-oxum.
   * To check, run {@link BagVerifier#canQuickVerify}
   */
  public void quicklyVerify(final Bag bag, final boolean ignoreHiddenFiles) throws IOException, InvalidPayloadOxumException, UnparsableVersionException, InvalidBagMetadataException{
    QuickVerifier.quicklyVerify(bag, ignoreHiddenFiles);
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
   * @throws MaliciousPathException if there is path that is referenced in the manifest that is outside the bag root directory
   * @throws VerificationException some other exception happened during processing so capture it here.
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   * @throws InvalidBagitFileFormatException if the manifest is not formatted properly
   */
  public void isValid(final Bag bag, final boolean ignoreHiddenFiles) throws IOException, NoSuchAlgorithmException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, FileNotInPayloadDirectoryException, InterruptedException, MaliciousPathException, CorruptChecksumException, VerificationException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
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
  void checkHashes(final Manifest manifest) throws CorruptChecksumException, InterruptedException, VerificationException{
    final CountDownLatch latch = new CountDownLatch( manifest.getFileToChecksumMap().size());
    final List<Exception> exceptions = new ArrayList<>(); //TODO maybe return all of these at some point...
    
    for(final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
      executor.execute(new CheckManifestHashsTask(entry, manifest.getAlgorithm().getMessageDigestName(), latch, exceptions));
    }
    
    latch.await();
    
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
   * <li>every file in the payload directory must be listed in at least one payload manifest
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
   * @throws MaliciousPathException if there is path that is referenced in the manifest that is outside the bag root directory
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   * @throws InvalidBagitFileFormatException if the manifest is not formatted properly 
   */
  public void isComplete(final Bag bag, final boolean ignoreHiddenFiles) throws 
    IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, 
    FileNotInPayloadDirectoryException, InterruptedException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    logger.info("Checking if the bag with root directory [{}] is complete.", bag.getRootDir());
    
    MandatoryVerifier.checkFetchItemsExist(bag.getItemsToFetch(), bag.getRootDir());
    
    MandatoryVerifier.checkBagitFileExists(bag.getRootDir(), bag.getVersion());
    
    MandatoryVerifier.checkPayloadDirectoryExists(bag);
    
    MandatoryVerifier.checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion());
    
    manifestVerifier.verifyPayload(bag, ignoreHiddenFiles);
  }
  
  public ExecutorService getExecutor() {
    return executor;
  }

  public PayloadVerifier getManifestVerifier() {
    return manifestVerifier;
  }
}
