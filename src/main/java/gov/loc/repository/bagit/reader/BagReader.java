package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.verify.PayloadFileExistsInManifestVistor;

/**
 * Responsible for reading a bag from the filesystem.
 */
public class BagReader {
  private static final Logger logger = LoggerFactory.getLogger(PayloadFileExistsInManifestVistor.class);
  
  /**
   * Read the bag from the filesystem and create a bag object 
   * @throws IOException 
   */
  public static Bag read(File rootDir) throws IOException{
    File bagitFile = new File(rootDir, "bagit.txt");
    Bag bag = readBagitTextFile(bagitFile, new Bag());
    bag.setRootDir(rootDir);
    
    bag = readAllManifests(rootDir, bag);
    
    bag = readBagMetadata(rootDir, bag);
    
    File fetchFile = new File(rootDir, "fetch.txt");
    if(fetchFile.exists()){
      bag = readFetch(fetchFile, bag);
    }
    
    return bag;
  }
  
  /**
   * Read the bagit.txt file and add it to the given bag. 
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param bagitFile
   * @param bag
   * @return
   * @throws IOException
   */
  public static Bag readBagitTextFile(File bagitFile, Bag bag) throws IOException{
    logger.debug("Reading bagit.txt file");
    LinkedHashMap<String, String> map = readKeyValueMapFromFile(bagitFile, ":");
    
    String version = map.get("BagIt-Version");
    logger.debug("BagIt-Version is [{}]", version);
    String encoding = map.get("Tag-File-Character-Encoding");
    logger.debug("Tag-File-Character-Encoding is [{}]", encoding);
    
    Bag newBag = new Bag(bag);
    newBag.setVersion(version);
    newBag.setFileEncoding(encoding);
    
    return newBag;
  }
  
  /**
   * Finds and reads all manifest files in the rootDir and adds them to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param rootDir
   * @param bag
   * @return
   * @throws IOException
   */
  public static Bag readAllManifests(File rootDir, Bag bag) throws IOException{
    Bag newBag = new Bag(bag);
    File[] files = getAllManifestFiles(rootDir);
    
    for(File file : files){
      if(file.getName().startsWith("tag")){
        newBag.getTagManifests().add(readManifest(file));
      }
      else if(file.getName().startsWith("manifest")){
        newBag.getPayLoadManifests().add(readManifest(file));
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
   * @param manifestFile
   * @return
   * @throws IOException
   */
  public static Manifest readManifest(File manifestFile) throws IOException{
    logger.debug("Reading manifest [{}]", manifestFile);
    String alg = manifestFile.getName().split("[-\\.]")[1];
    Manifest manifest = new Manifest(alg);
    
    HashMap<File, String> filetToChecksumMap = readChecksumFileMap(manifestFile);
    manifest.setFileToChecksumMap(filetToChecksumMap);
    
    return manifest;
  }
  
  protected static HashMap<File, String> readChecksumFileMap(File manifestFile) throws IOException{
    HashMap<File, String> map = new HashMap<>();
    BufferedReader br = Files.newBufferedReader(Paths.get(manifestFile.toURI()));

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split("\\s+");
      File file = new File(manifestFile.getParentFile(), parts[1]);
      logger.debug("Read checksum [{}] and file [{}] from manifest [{}]", parts[1], file, manifestFile);
      map.put(file, parts[0]);
      line = br.readLine();
    }
    
    return map;
  }
  
  /**
   * Reads the bag metadata file (bag-info.txt or package-info.txt) and adds it to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param rootDir
   * @param bag
   * @return
   * @throws IOException
   */
  public static Bag readBagMetadata(File rootDir, Bag bag) throws IOException{
    Bag newBag = new Bag(bag);
    LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
    
    File bagInfoFile = new File(rootDir, "bag-info.txt");
    if(bagInfoFile.exists()){
      metadata = readKeyValueMapFromFile(bagInfoFile, ":");
    }
    File packageInfoFile = new File(rootDir, "package-info.txt"); //onlu exists in versions 0.93 - 0.95
    if(packageInfoFile.exists()){
      metadata = readKeyValueMapFromFile(packageInfoFile, ":");
    }
    
    newBag.setMetadata(metadata);
    
    return newBag;
  }
  
  /**
   * Reads a fetch.txt file and adds {@link FetchItem} to the given bag.
   * Returns a <b>new</b> {@link Bag} object so that it is thread safe.
   * @param fetchFile
   * @param bag
   * @return
   * @throws IOException
   */
  public static Bag readFetch(File fetchFile, Bag bag) throws IOException{
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
  
  protected static LinkedHashMap<String, String> readKeyValueMapFromFile(File file, String splitRegex) throws IOException{
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    BufferedReader br = Files.newBufferedReader(Paths.get(file.toURI()));
    String lastEnteredKey = "";

    String line = br.readLine();
    while(line != null){
      if(line.matches("^\\s+.*")){
        logger.debug("Found an indented line - merging it to key [{}]", lastEnteredKey);
        map.merge(lastEnteredKey, System.lineSeparator() + line, String::concat);
      }
      else{
        String[] parts = line.split(splitRegex);
        lastEnteredKey = parts[0].trim();
        String value = parts[1].trim();
        logger.debug("Found key [{}] value [{}] in file [{}] using regex [{}]", lastEnteredKey, value, file, splitRegex);
        map.put(lastEnteredKey, value);
      }
       
      line = br.readLine();
    }
    
    return map;
  }
}
