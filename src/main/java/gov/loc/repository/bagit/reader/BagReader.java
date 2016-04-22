package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.MaliciousManifestException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.util.PathUtils;
import gov.loc.repository.bagit.verify.PayloadFileExistsInManifestVistor;
import javafx.util.Pair;

/**
 * Responsible for reading a bag from the filesystem.
 */
public class BagReader {
  private static final Logger logger = LoggerFactory.getLogger(PayloadFileExistsInManifestVistor.class);
  
  private BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping;
  
  public BagReader(){
    this.nameMapping = new StandardBagitAlgorithmNameToSupportedAlgorithmMapping();
  }
  
  public BagReader(BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    this.nameMapping = nameMapping;
  }
  
  /**
   * Read the bag from the filesystem and create a bag object
   * 
   * @param rootDir the root directory of the bag 
   * @throws IOException if there is a problem reading a file
   * @return a {@link Bag} object representing a bag on the filesystem
   * 
   * @throws UnparsableVersionException If there is a problem parsing the bagit version
   * @throws MaliciousManifestException if there is path that is referenced in the manifest that is outside the bag root directory
   * @throws InvalidBagMetadataException if the metadata or bagit.txt file does not conform to the bagit spec
   */
  public Bag read(Path rootDir) throws IOException, UnparsableVersionException, MaliciousManifestException, InvalidBagMetadataException{
    //@Incubating
    Path bagitDir = rootDir.resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = rootDir;
    }
    
    Path bagitFile = bagitDir.resolve("bagit.txt");
    Bag bag = readBagitTextFile(bagitFile, new Bag());
    bag.setRootDir(rootDir);
    
    bag = readAllManifests(bagitDir, bag);
    
    bag = readBagMetadata(bagitDir, bag);
    
    Path fetchFile = bagitDir.resolve("fetch.txt");
    if(Files.exists(fetchFile)){
      bag = readFetch(fetchFile, bag);
    }
    
    return bag;
  }
  
  /**
   * Read the bagit.txt file and add it to the given bag. 
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * 
   * @param bagitFile the bagit.txt file
   * @param bag the bag to include in the newly generated bag
   * @return a new bag containing the bagit.txt info
   * 
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   * @throws InvalidBagMetadataException if the bagit.txt file does not conform to the bagit spec
   */
  public Bag readBagitTextFile(Path bagitFile, Bag bag) throws IOException, UnparsableVersionException, InvalidBagMetadataException{
    logger.debug("Reading bagit.txt file");
    List<Pair<String, String>> pairs = readKeyValuesFromFile(bagitFile, ":");
    
    String version = "";
    String encoding = "";
    for(Pair<String, String> pair : pairs){
      if("BagIt-Version".equals(pair.getKey())){
        version = pair.getValue();
        logger.debug("BagIt-Version is [{}]", version);
      }
      if("Tag-File-Character-Encoding".equals(pair.getKey())){
        encoding = pair.getValue();
        logger.debug("Tag-File-Character-Encoding is [{}]", encoding);
      }
    }
    
    Bag newBag = new Bag(bag);
    newBag.setVersion(parseVersion(version));
    newBag.setFileEncoding(encoding);
    
    return newBag;
  }
  
  protected Version parseVersion(String version) throws UnparsableVersionException{
    if(!version.contains(".")){
      throw new UnparsableVersionException("Version must be in format MAJOR.MINOR but was " + version);
    }
    
    String[] parts = version.split("\\.");
    int major = Integer.parseInt(parts[0]);
    int minor = Integer.parseInt(parts[1]);
    
    return new Version(major, minor);
  }
  
  /**
   * Finds and reads all manifest files in the rootDir and adds them to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * 
   * @param rootDir the parent directory of the manifest(s)
   * @param bag the bag to include in the new bag
   * @return a new bag that contains all the manifest(s) information
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousManifestException if there is path that is referenced in the manifest that is outside the bag root directory
   */
  public Bag readAllManifests(Path rootDir, Bag bag) throws IOException, MaliciousManifestException{
    logger.info("Attempting to find and read manifests");
    Bag newBag = new Bag(bag);
    DirectoryStream<Path> manifests = getAllManifestFiles(rootDir);
    
    for (Path path : manifests){
      String filename = PathUtils.getFilename(path);
      
      if(filename.startsWith("tagmanifest-")){
        logger.debug("Found tag manifest [{}]", path);
        newBag.getTagManifests().add(readManifest(path, bag.getRootDir()));
      }
      else if(filename.startsWith("manifest-")){
        logger.debug("Found payload manifest [{}]", path);
        newBag.getPayLoadManifests().add(readManifest(path, bag.getRootDir()));
      }
    }
    
    return newBag;
  }
  
  protected DirectoryStream<Path> getAllManifestFiles(Path rootDir) throws IOException{
    DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
      public boolean accept(Path file) throws IOException {
        if(file == null || file.getFileName() == null){ return false;}
        String filename = PathUtils.getFilename(file);
        return filename.startsWith("tagmanifest-") || filename.startsWith("manifest-");
      }
    };
    
    return Files.newDirectoryStream(rootDir, filter);
  }
  
  /**
   * Reads a manifest file and converts it to a {@link Manifest} object.
   * 
   * @param manifestFile a specific manifest file
   * @param bagRootDir the root directory of the bag
   * @return the converted manifest object from the file
   * 
   * @throws IOException if there is a problem reading a file
   * @throws MaliciousManifestException if there is path that is referenced in the manifest that is outside the bag root directory
   */
  public Manifest readManifest(Path manifestFile, Path bagRootDir) throws IOException, MaliciousManifestException{
    logger.debug("Reading manifest [{}]", manifestFile);
    String alg = PathUtils.getFilename(manifestFile).split("[-\\.]")[1];
    SupportedAlgorithm algorithm = nameMapping.getMessageDigestName(alg);
    
    Manifest manifest = new Manifest(algorithm);
    
    HashMap<Path, String> filetToChecksumMap = readChecksumFileMap(manifestFile, bagRootDir);
    manifest.setFileToChecksumMap(filetToChecksumMap);
    
    return manifest;
  }
  
  protected HashMap<Path, String> readChecksumFileMap(Path manifestFile, Path bagRootDir) throws IOException, MaliciousManifestException{
    HashMap<Path, String> map = new HashMap<>();
    BufferedReader br = Files.newBufferedReader(manifestFile);

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split("\\s+", 2);
      Path file = bagRootDir.resolve(parts[1]);
      if(!file.normalize().startsWith(bagRootDir)){
        throw new MaliciousManifestException("Path " + file + " is outside the bag root directory of " + bagRootDir + 
            "! This is not allowed according to the bagit specification!");
      }
      logger.debug("Read checksum [{}] and file [{}] from manifest [{}]", parts[0], file, manifestFile);
      map.put(file, parts[0]);
      line = br.readLine();
    }
    
    return map;
  }
  
  /**
   * Reads the bag metadata file (bag-info.txt or package-info.txt) and adds it to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * 
   * @param rootDir the root directory of the bag
   * @param bag the bag to include in the new bag
   * @return a new bag that contains the bag-info.txt (metadata) information
   * 
   * @throws IOException if there is a problem reading a file
   * @throws InvalidBagMetadataException if the metadata file does not conform to the bagit spec
   */
  public Bag readBagMetadata(Path rootDir, Bag bag) throws IOException, InvalidBagMetadataException{
    //TODO update for .bagit being yaml...
    logger.info("Attempting to read bag metadata file");
    Bag newBag = new Bag(bag);
    List<Pair<String, String>> metadata = new ArrayList<>();
    
    Path bagInfoFile = rootDir.resolve("bag-info.txt");
    if(Files.exists(bagInfoFile)){
      logger.debug("Found [{}] file", bagInfoFile);
      metadata = readKeyValuesFromFile(bagInfoFile, ":");
    }
    Path packageInfoFile = rootDir.resolve("package-info.txt"); //only exists in versions 0.93 - 0.95
    if(Files.exists(packageInfoFile)){
      logger.debug("Found [{}] file", packageInfoFile);
      metadata = readKeyValuesFromFile(packageInfoFile, ":");
    }
    
    newBag.setMetadata(metadata);
    
    return newBag;
  }
  
  /**
   * Reads a fetch.txt file and adds {@link FetchItem} to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * 
   * @param fetchFile the specific fetch file
   * @param bag the bag to include in the new bag
   * @return a new bag that contains a list of items to fetch
   * 
   * @throws IOException if there is a problem reading a file
   */
  public Bag readFetch(Path fetchFile, Bag bag) throws IOException{
    logger.info("Attempting to read [{}]", fetchFile);
    Bag newBag = new Bag(bag);
    BufferedReader br = Files.newBufferedReader(fetchFile);

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split("\\s+", 3);
      long length = parts[1].equals("-") ? -1 : Long.decode(parts[1]);
      URL url = new URL(parts[0]);
      
      logger.debug("Read URL [{}] length [{}] path [{}] from fetch file [{}]", url, length, parts[2], fetchFile);
      FetchItem itemToFetch = new FetchItem(url, length, parts[2]);
      newBag.getItemsToFetch().add(itemToFetch);
      
      line = br.readLine();
    }

    return newBag;
  }
  
  protected List<Pair<String, String>> readKeyValuesFromFile(Path file, String splitRegex) throws IOException, InvalidBagMetadataException{
    List<Pair<String, String>> keyValues = new ArrayList<>();
    BufferedReader br = Files.newBufferedReader(file);

    String line = br.readLine();
    while(line != null){
      if(line.matches("^\\s+.*")){
        Pair<String, String> oldKeyValue = keyValues.remove(keyValues.size() -1);
        Pair<String, String> newKeyValue = new Pair<String, String>(oldKeyValue.getKey(), oldKeyValue.getValue() + System.lineSeparator() +line);
        keyValues.add(newKeyValue);
        
        logger.debug("Found an indented line - merging it with key [{}]", oldKeyValue.getKey());
      }
      else{
        String[] parts = line.split(splitRegex, 2);
        if(parts.length != 2){
          StringBuilder message = new StringBuilder();
          message.append("Line ").append('[').append(line)
            .append("] does not meet the bagit specification for a bag tag file. Perhaps you meant to indent it ")
            .append("by a space or a tab? Or perhaps you didn't use a colon to separate the key from the value?")
            .append("It must follow the form of <key>:<value> or if continuing from another line must be indented ")
            .append("by a space or a tab.");
          
          throw new InvalidBagMetadataException(message.toString());
        }
        String key = parts[0].trim();
        String value = parts[1].trim();
        logger.debug("Found key [{}] value [{}] in file [{}] using regex [{}]", key, value, file, splitRegex);
        keyValues.add(new Pair<String, String>(key, value));
      }
       
      line = br.readLine();
    }
    
    return keyValues;
  }
}
