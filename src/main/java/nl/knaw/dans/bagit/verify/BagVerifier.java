/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.Manifest;
import nl.knaw.dans.bagit.exceptions.CorruptChecksumException;
import nl.knaw.dans.bagit.exceptions.FileNotInManifestException;
import nl.knaw.dans.bagit.exceptions.FileNotInPayloadDirectoryException;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.InvalidPayloadOxumException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import nl.knaw.dans.bagit.exceptions.MissingBagitFileException;
import nl.knaw.dans.bagit.exceptions.MissingPayloadDirectoryException;
import nl.knaw.dans.bagit.exceptions.MissingPayloadManifestException;
import nl.knaw.dans.bagit.exceptions.PayloadOxumDoesNotExistException;
import nl.knaw.dans.bagit.exceptions.UnsupportedAlgorithmException;
import nl.knaw.dans.bagit.exceptions.VerificationException;
import nl.knaw.dans.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import nl.knaw.dans.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public final class BagVerifier implements AutoCloseable{
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  private final ManifestVerifier manifestVerifier;
  private final ExecutorService executor;
  
  /**
   * Create a BagVerifier with a cached thread pool and a 
   * {@link StandardBagitAlgorithmNameToSupportedAlgorithmMapping}
   */
  public BagVerifier(){
    this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  /**
   * Create a BagVerifier with a cached thread pool and a custom mapping
   * 
   * @param nameMapping the mapping between BagIt algorithm name and the java supported algorithm
   */
  public BagVerifier(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), nameMapping);
  }
  
  /**
   * Create a BagVerifier with a custom thread pool and a 
   * {@link StandardBagitAlgorithmNameToSupportedAlgorithmMapping}
   * 
   * @param executor the thread pool to use when doing work
   */
  public BagVerifier(final ExecutorService executor){
    this(executor, new StandardBagitAlgorithmNameToSupportedAlgorithmMapping());
  }
  
  /**
   * Create a BagVerifier with a custom thread pool and a custom mapping
   * 
   * @param nameMapping the mapping between BagIt algorithm name and the java supported algorithm
   * @param executor the thread pool to use when doing work
   */
  public BagVerifier(final ExecutorService executor, final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    manifestVerifier = new ManifestVerifier(nameMapping, executor);
    this.executor = executor;
  }
  
  @Override
  public void close() throws SecurityException{
    //shutdown the thread pool so the resource isn't leaked
    executor.shutdown();
    manifestVerifier.close();
  }
  
  /**
   * Determine if we can quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the {@link Bag} object you wish to check
   * @return true if the bag can be quickly verified
   */
  public static boolean canQuickVerify(final Bag bag){
    return QuickVerifier.canQuickVerify(bag);
  }
  
  /**
   * Quickly verify by comparing the number of files and the total number of bytes expected
   * 
   * @param bag the bag to verify by payload-oxum
   * 
   * @throws IOException if there is an error reading a file
   * @throws InvalidPayloadOxumException if either the total bytes or the number of files
   * calculated for the payload directory of the bag is different than the supplied values
   * @throws PayloadOxumDoesNotExistException if the bag does not contain a payload-oxum.
   * To check, run {@link BagVerifier#canQuickVerify}
   */
  public static void quicklyVerify(final Bag bag) throws IOException, InvalidPayloadOxumException{
    QuickVerifier.quicklyVerify(bag);
  }

  /**
   * See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a><br>
   *  A bag is <b>valid</b> if the bag is complete and every checksum has been 
   *  verified against the contents of its corresponding file.
   * 
   * @param bag the {@link Bag} object to check
   * @param ignoreHiddenFiles ignore hidden files unless explicitly listed in manifest(s)
   * 
   * @throws CorruptChecksumException when the computed hash doesn't match given hash
   * @throws IOException if there was an error with the file
   * @throws FileNotInManifestException if a file is found in the payload directory but not in manifest(s)
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
  public void isValid(final Bag bag, final boolean ignoreHiddenFiles) throws IOException, FileNotInManifestException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, FileNotInPayloadDirectoryException, InterruptedException, MaliciousPathException, CorruptChecksumException, VerificationException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    logger.info(messages.getString("checking_bag_is_valid"), bag.getRootDir());
    isComplete(bag, ignoreHiddenFiles);
    
    logger.debug(messages.getString("checking_payload_checksums"));
    for(final Manifest payloadManifest : bag.getPayLoadManifests()){
      checkHashes(payloadManifest);
    }
    
    logger.debug(messages.getString("checking_tag_file_checksums"));
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
    
    //TODO maybe return all of these at some point...
    final Collection<Exception> exceptions = Collections.synchronizedCollection(new ArrayList<>());
    
    for(final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
      executor.execute(new CheckManifestHashesTask(entry, manifest.getAlgorithm().getMessageDigestName(), latch, exceptions));
    }
    
    latch.await();
    
    if(!exceptions.isEmpty()){
      final Exception e = exceptions.iterator().next();
      if(e instanceof CorruptChecksumException){
        logger.debug(messages.getString("checksums_not_matching_error"), exceptions.size());
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
   * @throws MaliciousPathException if there is path that is referenced in the manifest that is outside the bag root directory
   * @throws UnsupportedAlgorithmException if the manifest uses a algorithm that isn't supported
   * @throws InvalidBagitFileFormatException if the manifest is not formatted properly 
   */
  public void isComplete(final Bag bag, final boolean ignoreHiddenFiles) throws 
    IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, 
    FileNotInPayloadDirectoryException, InterruptedException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    logger.info(messages.getString("checking_bag_is_complete"), bag.getRootDir());
    
    MandatoryVerifier.checkFetchItemsExist(bag.getItemsToFetch(), bag.getRootDir());
    
    MandatoryVerifier.checkBagitFileExists(bag.getRootDir(), bag.getVersion());
    
    MandatoryVerifier.checkPayloadDirectoryExists(bag);
    
    MandatoryVerifier.checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), bag.getVersion());
    
    manifestVerifier.verifyManifests(bag, ignoreHiddenFiles);
  }
  
  public ExecutorService getExecutor() {
    return executor;
  }

  public ManifestVerifier getManifestVerifier() {
    return manifestVerifier;
  }
}
