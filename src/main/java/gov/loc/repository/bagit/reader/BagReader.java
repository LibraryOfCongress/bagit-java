package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.verify.PayloadFileExistsInManifestVistor;
import javafx.util.Pair;

/**
 * Responsible for reading a bag from the filesystem.
 */
public class BagReader {
  private static final Logger logger = LoggerFactory.getLogger(PayloadFileExistsInManifestVistor.class);
  
  /**
   * Read the bag from the filesystem and create a bag object
   * @param rootDir the root directory of the bag 
   * @throws IOException if there is a problem reading a file
   * @return a {@link Bag} object representing a bag on the filesystem
   * @throws UnparsableVersionException If there is a problem parsing the bagit version
   */
  public static Bag read(File rootDir) throws IOException, UnparsableVersionException{
    File bagitDir = new File(rootDir, ".bagit");
    if(!bagitDir.exists()){
      bagitDir = rootDir;
    }
    
    File bagitFile = new File(bagitDir, "bagit.txt");
    Bag bag = readBagitTextFile(bagitFile, new Bag());
    bag.setRootDir(rootDir);
    
    bag = readAllManifests(bagitDir, bag);
    
    bag = readBagMetadata(bagitDir, bag);
    
    File fetchFile = new File(bagitDir, "fetch.txt");
    if(fetchFile.exists()){
      bag = readFetch(fetchFile, bag);
    }
    
    return bag;
  }
  
  /**
   * Read the bagit.txt file and add it to the given bag. 
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param bagitFile the bagit.txt file
   * @param bag the bag to include in the newly generated bag
   * @return a new bag containing the bagit.txt info
   * @throws IOException if there is a problem reading a file
   * @throws UnparsableVersionException if there is a problem parsing the bagit version number
   */
  public static Bag readBagitTextFile(File bagitFile, Bag bag) throws IOException, UnparsableVersionException{
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
  
  protected static Version parseVersion(String version) throws UnparsableVersionException{
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
   * @param rootDir the parent directory of the manifest(s)
   * @param bag the bag to include in the new bag
   * @return a new bag that contains all the manifest(s) information
   * @throws IOException if there is a problem reading a file
   */
  public static Bag readAllManifests(File rootDir, Bag bag) throws IOException{
    logger.info("Attempting to find and read manifests");
    Bag newBag = new Bag(bag);
    File[] files = getAllManifestFiles(rootDir);
    
    for(File file : files){
      if(file.getName().startsWith("tag")){
        logger.debug("Found tag manifest [{}]", file);
        newBag.getTagManifests().add(readManifest(file, bag.getRootDir()));
      }
      else if(file.getName().startsWith("manifest")){
        logger.debug("Found payload manifest [{}]", file);
        newBag.getPayLoadManifests().add(readManifest(file, bag.getRootDir()));
      }
    }
    
    return newBag;
  }
  
  protected static File[] getAllManifestFiles(File rootDir){
    File[] files = rootDir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.matches("(tag)?manifest\\-.*\\.txt");
      }
    });
    
    return files == null? new File[]{} : files;
  }
  
  /**
   * Reads a manifest file and converts it to a {@link Manifest} object.
   * @param manifestFile a specific manifest file
   * @param bagRootDir the root directory of the bag
   * @return the converted manifest object from the file
   * @throws IOException if there is a problem reading a file
   */
  public static Manifest readManifest(File manifestFile, File bagRootDir) throws IOException{
    logger.debug("Reading manifest [{}]", manifestFile);
    String alg = manifestFile.getName().split("[-\\.]")[1];
    StandardSupportedAlgorithms algorithm = StandardSupportedAlgorithms.valueOf(alg.toUpperCase());
    
    Manifest manifest = new Manifest(algorithm);
    
    HashMap<File, String> filetToChecksumMap = readChecksumFileMap(manifestFile, bagRootDir);
    manifest.setFileToChecksumMap(filetToChecksumMap);
    
    return manifest;
  }
  
  protected static HashMap<File, String> readChecksumFileMap(File manifestFile, File bagRootDir) throws IOException{
    HashMap<File, String> map = new HashMap<>();
    BufferedReader br = Files.newBufferedReader(Paths.get(manifestFile.toURI()));

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split("\\s+", 2);
      File file = new File(bagRootDir, parts[1]);
      logger.debug("Read checksum [{}] and file [{}] from manifest [{}]", parts[0], file, manifestFile);
      map.put(file, parts[0]);
      line = br.readLine();
    }
    
    return map;
  }
  
  /**
   * Reads the bag metadata file (bag-info.txt or package-info.txt) and adds it to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param rootDir the root directory of the bag
   * @param bag the bag to include in the new bag
   * @return a new bag that contains the bag-info.txt (metadata) information
   * @throws IOException if there is a problem reading a file
   */
  public static Bag readBagMetadata(File rootDir, Bag bag) throws IOException{
    logger.info("Attempting to read bag metadata file");
    Bag newBag = new Bag(bag);
    List<Pair<String, String>> metadata = new ArrayList<>();
    
    File bagInfoFile = new File(rootDir, "bag-info.txt");
    if(bagInfoFile.exists()){
      logger.debug("Found [{}] file", bagInfoFile);
      metadata = readKeyValuesFromFile(bagInfoFile, ":");
    }
    File packageInfoFile = new File(rootDir, "package-info.txt"); //onlu exists in versions 0.93 - 0.95
    if(packageInfoFile.exists()){
      logger.debug("Found [{}] file", packageInfoFile);
      metadata = readKeyValuesFromFile(packageInfoFile, ":");
    }
    
    newBag.setMetadata(metadata);
    
    return newBag;
  }
  
  /**
   * Reads a fetch.txt file and adds {@link FetchItem} to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param fetchFile the specific fetch file
   * @param bag the bag to include in the new bag
   * @return a new bag that contains a list of items to fetch
   * @throws IOException if there is a problem reading a file
   */
  public static Bag readFetch(File fetchFile, Bag bag) throws IOException{
    logger.info("Attempting to read [{}]", fetchFile);
    Bag newBag = new Bag(bag);
    BufferedReader br = Files.newBufferedReader(Paths.get(fetchFile.toURI()));

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
  
  protected static List<Pair<String, String>> readKeyValuesFromFile(File file, String splitRegex) throws IOException{
    List<Pair<String, String>> keyValues = new ArrayList<>();
    BufferedReader br = Files.newBufferedReader(Paths.get(file.toURI()));

    String line = br.readLine();
    while(line != null){
      if(line.matches("^\\s+.*")){
        
        Pair<String, String> oldKeyValue = keyValues.remove(keyValues.size() -1);
        Pair<String, String> newKeyValue = new Pair<String, String>(oldKeyValue.getKey(), oldKeyValue.getValue() + System.lineSeparator() +line);
        keyValues.add(newKeyValue);
        
        logger.debug("Found an indented line - merging it with key [{}]", oldKeyValue.getKey());
      }
      else{
        String[] parts = line.split(splitRegex);
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
