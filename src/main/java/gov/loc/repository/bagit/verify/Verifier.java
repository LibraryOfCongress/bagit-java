package gov.loc.repository.bagit.verify;

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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.SupportedAlgorithms;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.hash.Hasher;
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
   * @throws FileNotInPayloadDirectoryException 
   * @throws MissingPayloadDirectoryException 
   * @throws MissingBagitFileException 
   * @throws MissingPayloadManifestException 
   * @throws CorruptChecksumException 
   */
  //TODO make multithreaded?
  public static boolean isValid(Bag bag, boolean ignoreHiddenFiles) throws 
    NoSuchAlgorithmException, IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, FileNotInPayloadDirectoryException, CorruptChecksumException{
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
    
    return true;
  }
  
  /**
   * @throws CorruptChecksumException if any of the files computed checksum is different than the manifest supplied checksum 
   */
  protected static void checkHashes(Manifest manifest) throws NoSuchAlgorithmException, IOException, CorruptChecksumException{
    SupportedAlgorithms algorithm = SupportedAlgorithms.valueOf(manifest.getAlgorithm().toUpperCase());
    logger.debug("Checking manifest using algorithm {}", algorithm.getMessageDigestName());
    
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    for(Entry<File, String> entry : manifest.getFileToChecksumMap().entrySet()){
      checkManifestEntry(entry, messageDigest, manifest.getAlgorithm());
    }
  }
  
  protected static void checkManifestEntry(Entry<File, String> entry, MessageDigest messageDigest, String algorithm) throws IOException, CorruptChecksumException{
    if(entry.getKey().exists()){
      logger.debug("Checking file [{}] to see if checksum matches [{}]", entry.getKey(), entry.getValue());
      InputStream inputStream = Files.newInputStream(Paths.get(entry.getKey().toURI()), StandardOpenOption.READ);
      String hash = Hasher.hash(inputStream, messageDigest);
      if(!hash.equals(entry.getValue())){
        throw new CorruptChecksumException("File [" + entry.getKey() + "] is suppose to have a " + algorithm + 
            " hash of [" + entry.getValue() + "] but was computed [" + hash+"]");
      }
    }
    //if the file doesn't exist it will be caught by checkAllFilesListedInManifestExist method
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
   * @throws IOException if there was an error with the file
   * @throws MissingPayloadManifestException if there is not at least one payload manifest
   * @throws MissingBagitFileException  if there is no bagit.txt file
   * @throws MissingPayloadDirectoryException if there is no /data directory
   * @throws FileNotInPayloadDirectoryException if a manifest lists a file but it is not in the payload directory
   */
  public static boolean isComplete(Bag bag, boolean ignoreHiddenFiles) throws 
    IOException, MissingPayloadManifestException, MissingBagitFileException, MissingPayloadDirectoryException, FileNotInPayloadDirectoryException{
    logger.info("Checking if the bag with root directory [{}] is complete.", bag.getRootDir());
    
    File bagitFile = new File(bag.getRootDir(), "bagit.txt");
    if(!bagitFile.exists()){
      throw new MissingBagitFileException("File [" + bagitFile + "] should exist but it doesn't");
    }
    
    File dataDir = new File(bag.getRootDir(), "data");
    if(!dataDir.exists()){
      throw new MissingPayloadDirectoryException("File [" + dataDir + "] should exist but it doesn't");
    }
    
    checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir());
    
    Set<File> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    checkAllFilesListedInManifestExist(allFilesListedInManifests);
    checkAllFilesInPayloadDirAreListedInAManifest(allFilesListedInManifests, dataDir, ignoreHiddenFiles);
    
    return true;
  }
  
  private static void checkIfAtLeastOnePayloadManifestsExist(File rootDir) throws MissingPayloadManifestException{
    boolean hasAtLeastOneManifest = false;
    for(String filename : rootDir.list()){
      if(filename.matches("manifest\\-.*\\.txt")){
        logger.debug("Found payload manifest file [{}]", filename);
        hasAtLeastOneManifest = true;
      }
    }
    
    if(!hasAtLeastOneManifest){
      throw new MissingPayloadManifestException("Bag does not contain any payload manifest files");
    }
    
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
  
  protected static void checkAllFilesListedInManifestExist(Set<File> files) throws FileNotInPayloadDirectoryException{
    logger.debug("Checking if all files listed in the manifest(s) exist");
    for(File file : files){
      if(!file.exists()){
        throw new FileNotInPayloadDirectoryException("Manifest lists file [" + file + "] but it does not exist");
      }
    }
  }
  
  protected static void checkAllFilesInPayloadDirAreListedInAManifest(Set<File> filesListedInManifests, File payloadDir, boolean ignoreHiddenFiles) throws IOException{
    logger.debug("Checking if all payload files (files in /data dir) are listed in at least one manifest");
    if(payloadDir.exists()){
      Path start = Paths.get(payloadDir.toURI());
      Files.walkFileTree(start, new PayloadFileExistsInManifestVistor(filesListedInManifests, ignoreHiddenFiles));
    }
  }
}
