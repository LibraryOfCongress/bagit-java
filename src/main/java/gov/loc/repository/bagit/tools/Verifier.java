package gov.loc.repository.bagit.tools;

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
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.VerifyResponse;
import gov.loc.repository.bagit.reader.BagReader;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public class Verifier {

  /**
   *  See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a></br>
   *  A bag is <b>valid</b> if the bag is complete and every checksum has been 
   *  verified against the contents of its corresponding file.
   * @throws IOException 
   * @throws NoSuchAlgorithmException 
   */
  //TODO make multithreaded?
  public static VerifyResponse isValid(Bag bag) throws NoSuchAlgorithmException, IOException{
    VerifyResponse response = isComplete(bag);
    
    for(Manifest payloadManifest : bag.getPayLoadManifests()){
      List<String> errorMessages = checkHashes(payloadManifest);
      if(errorMessages.size() > 0){
        response.setErrored(true);
        response.getErrorMessages().addAll(errorMessages);
      }
    }
    
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
    
    MessageDigest messageDigest = MessageDigest.getInstance(manifest.getAlgorithm());
    for(Entry<File, String> entry : manifest.getFileToChecksumMap().entrySet()){
      if(entry.getKey().exists()){
        InputStream inputStream = Files.newInputStream(Paths.get(entry.getKey().toURI()), StandardOpenOption.READ);
        String hash = hash(inputStream, messageDigest);
        if(!hash.equals(entry.getValue())){
          messages.add("File [" + entry.getKey() + "] is suppose to have a " + manifest.getAlgorithm() + 
              " hash of [" + entry.getValue() + "] but was computed [" + hash+"]");
        }
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
  public static VerifyResponse isComplete(Bag bag) throws IOException{
    VerifyResponse response = new VerifyResponse();
    
    File bagitFile = new File(bag.getRootDir(), "bagit.txt");
    if(!bagitFile.exists()){
      response.setErrored(true);
      response.getErrorMessages().add("File [" + bagitFile + "] should exist but it doesn't");
    }
    
    File dataDir = new File(bag.getRootDir(), "data");
    if(!dataDir.exists()){
      response.setErrored(true);
      response.getErrorMessages().add("File [" + dataDir + "] should exist but it doesn't");
    }
    
    response = checkIfAtLeastOnePayloadManifestsExist(bag.getRootDir(), response);
    
    Set<File> allFilesListedInManifests = getAllFilesListedInManifests(bag);
    response = checkAllFilesListedInManifestExist(allFilesListedInManifests, response);
    response = checkAllFilesInPayloadDirAreListedInAManifest(response, allFilesListedInManifests, dataDir);
    
    return response;
  }
  
  private static VerifyResponse checkIfAtLeastOnePayloadManifestsExist(File rootDir, VerifyResponse response){
    boolean hasAtLeastOneManifest = false;
    for(String filename : rootDir.list()){
      if(filename.matches("manifest\\-.*\\.txt")){
        hasAtLeastOneManifest = true;
      }
    }
    
    if(!hasAtLeastOneManifest){
      response.setErrored(true);
      response.getErrorMessages().add("Bag does not contain any payload manifest files!");
    }
    
    return response;
  }
  
  protected static Set<File> getAllFilesListedInManifests(Bag bag) throws IOException{
    Set<File> filesListedInManifests = new HashSet<>();
    
    File[] files = bag.getRootDir().listFiles();
    for(File file : files){
      if(file.getName().matches("(tag)?manifest\\-.*\\.txt")){
        Manifest manifest = BagReader.readManifest(file);
        filesListedInManifests.addAll(manifest.getFileToChecksumMap().keySet());
      }
    }
    
    return filesListedInManifests;
  }
  
  protected static VerifyResponse checkAllFilesListedInManifestExist(Set<File> files, VerifyResponse response){
    for(File file : files){
      if(!file.exists()){
        response.setErrored(true);
        response.getErrorMessages().add("Bag lists file [" + file + "] in manifest but it does not exist");
      }
    }
    
    return response;
  }
  
  protected static VerifyResponse checkAllFilesInPayloadDirAreListedInAManifest(VerifyResponse response, Set<File> filesListedInManifests, File payloadDir) throws IOException{
    if(payloadDir.exists()){
      Path start = Paths.get(payloadDir.toURI());
      Files.walkFileTree(start, new PayloadFileExistsInManifestVistor(filesListedInManifests, response));
    }
    
    return response;
  }
}
