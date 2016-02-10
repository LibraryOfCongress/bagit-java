package gov.loc.repository.bagit.verify;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.SupportedAlgorithms;
import gov.loc.repository.bagit.domain.SimpleResponse;
import gov.loc.repository.bagit.reader.BagReader;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public class Verifier {
  private static final Logger logger = LoggerFactory.getLogger(Verifier.class);
  
  static {
    logger.debug("Adding bouncy castle crypo provider to enable SHA3 support");
    Security.addProvider(new BouncyCastleProvider());
  }

  /**
   *  See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a></br>
   *  A bag is <b>valid</b> if the bag is complete and every checksum has been 
   *  verified against the contents of its corresponding file.
   * @throws IOException 
   * @throws NoSuchAlgorithmException 
   */
  //TODO make multithreaded?
  public static SimpleResponse isValid(Bag bag) throws NoSuchAlgorithmException, IOException{
    logger.info("Checking if the bag with root directory [{}] is valid.", bag.getRootDir());
    SimpleResponse response = isComplete(bag);
    
    logger.debug("Checking payload manifest(s) checksums");
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      List<String> errorMessages = checkHashes(payloadManifest);
      if(errorMessages.size() > 0){
        response.setErrored(true);
        response.getErrorMessages().addAll(errorMessages);
      }
    }
    
    logger.debug("Checking tag manifest(s) checksums");
    for(Manifest tagManifest : bag.getTagManifests()){
      List<String> errorMessages = checkHashes(tagManifest);
      if(errorMessages.size() > 0){
        response.setErrored(true);
        response.getErrorMessages().addAll(errorMessages);
      }
    }
    
    return response;
  }
  
  /**
   * returns a list of error messages or an empty list if none 
   */
  protected static List<String> checkHashes(Manifest manifest) throws NoSuchAlgorithmException, IOException{
    List<String> messages = new ArrayList<>();
    SupportedAlgorithms algorithm = SupportedAlgorithms.valueOf(manifest.getAlgorithm().toUpperCase());
    logger.debug("Checking manifest using algorithm {}", algorithm.getMessageDigestName());
    
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    for(Entry<File, String> entry : manifest.getFileToChecksumMap().entrySet()){
      if(entry.getKey().exists()){
        logger.debug("Checking file [{}] to see if checksum matches [{}]", entry.getKey(), entry.getValue());
        InputStream inputStream = Files.newInputStream(Paths.get(entry.getKey().toURI()), StandardOpenOption.READ);
        String hash = hash(inputStream, messageDigest);
        if(!hash.equals(entry.getValue())){
          logger.error("File [{}] is suppose to have a {} hash of [{}] but was computed to be [{}]", 
              entry.getKey(), algorithm, entry.getValue(), hash);
          messages.add("File [" + entry.getKey() + "] is suppose to have a " + manifest.getAlgorithm() + 
              " hash of [" + entry.getValue() + "] but was computed [" + hash+"]");
        }
      }
      else{
        logger.warn("File [{}] is listed in the manifest but doesn't exist on disk!", entry.getKey());
      }
    }
    
    return messages;
  }
  
  protected static String hash(final InputStream inputStream, final MessageDigest messageDigest) throws IOException {
    try (InputStream is = new BufferedInputStream(inputStream)) {
      final byte[] buffer = new byte[1024];
      for (int read = 0; (read = is.read(buffer)) != -1;) {
        messageDigest.update(buffer, 0, read);
      }
    }

    // Convert the byte to hex format
    return formatMessageDigest(messageDigest);
  }
  
  protected static String formatMessageDigest(final MessageDigest messageDigest){
    try (Formatter formatter = new Formatter()) {
      for (final byte b : messageDigest.digest()) {
        formatter.format("%02x", b);
      }
      return formatter.toString();
    }
  }
  
  /**
   * See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a></br>
   * A bag is <b>complete</b> if </br>
   * <p><ul>
   * <li>every element is present
   * <li>every file in the payload manifest(s) are present
   * <li>every file in the tag manifest(s) are present. Tag files not listed in a tag manifest may be present.
   * <li>every file in the data directory must be listed in at least one payload manifest
   * <li>each element must comply with the bagit spec
   * </ul></p>
   * @throws IOException 
   */
  public static SimpleResponse isComplete(Bag bag) throws IOException{
    logger.info("Checking if the bag with root directory [{}] is complete.", bag.getRootDir());
    SimpleResponse response = new SimpleResponse();
    
    File bagitFile = new File(bag.getRootDir(), "bagit.txt");
    if(!bagitFile.exists()){
      logger.error("File [{}] should exist but it doesn't", bagitFile);
      response.setErrored(true);
      response.getErrorMessages().add("File [" + bagitFile + "] should exist but it doesn't");
    }
    
    File dataDir = new File(bag.getRootDir(), "data");
    if(!dataDir.exists()){
      logger.error("File [{}] should exist but it doesn't", dataDir);
      response.setErrored(true);
      response.getErrorMessages().add("File [" + dataDir + "] should exist but it doesn't");
    }
    
    response = checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), response);
    
    Set<File> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    response = checkAllFilesListedInManifestExist(allFilesListedInManifests, response);
    response = checkAllFilesInPayloadDirAreListedInAManifest(response, allFilesListedInManifests, dataDir);
    
    return response;
  }
  
  private static SimpleResponse checkIfAtLeastOnePayloadManifestsExist(File rootDir, SimpleResponse response){
    boolean hasAtLeastOneManifest = false;
    for(String filename : rootDir.list()){
      if(filename.matches("manifest\\-.*\\.txt")){
        logger.debug("Found payload manifest file [{}]", filename);
        hasAtLeastOneManifest = true;
      }
    }
    
    if(!hasAtLeastOneManifest){
      logger.error("Bag does not contain any payload manifest files");
      response.setErrored(true);
      response.getErrorMessages().add("Bag does not contain any payload manifest files");
    }
    
    return response;
  }
  
  protected static Set<File> getAllFilesListedInManifests(Bag bag) throws IOException{
    Set<File> filesListedInManifests = new HashSet<>();
    
    File[] files = bag.getRootDir().listFiles();
    for(File file : files){
      if(file.getName().matches("(tag)?manifest\\-.*\\.txt")){
        logger.debug("Getting files and checksums listed in [{}]", file);
        Manifest manifest = BagReader.readManifest(file);
        filesListedInManifests.addAll(manifest.getFileToChecksumMap().keySet());
      }
    }
    
    return filesListedInManifests;
  }
  
  protected static SimpleResponse checkAllFilesListedInManifestExist(Set<File> files, SimpleResponse response){
    logger.debug("Checking if all files listed in the manifest(s) exist");
    for(File file : files){
      if(!file.exists()){
        logger.error("Manifest lists file [{}] but it does not exist", file);
        response.setErrored(true);
        response.getErrorMessages().add("Manifest lists file [" + file + "] but it does not exist");
      }
    }
    
    return response;
  }
  
  protected static SimpleResponse checkAllFilesInPayloadDirAreListedInAManifest(SimpleResponse response, Set<File> filesListedInManifests, File payloadDir) throws IOException{
    logger.debug("Checking if all payload files (files in /data dir) are listed in at least one manifest");
    if(payloadDir.exists()){
      Path start = Paths.get(payloadDir.toURI());
      Files.walkFileTree(start, new PayloadFileExistsInManifestVistor(filesListedInManifests, response));
    }
    
    return response;
  }
}
