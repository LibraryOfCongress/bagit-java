package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;

/**
 * responsible for reading a bag from the filesystem.
 */
//TODO add logic for versions other than 0.97
public class BagReader {
  /**
   * Read the bag from the filesystem and create a bag object 
   * @throws IOException 
   */
  //TODO read in parallel
  public static Bag read(File rootDir) throws IOException{
    File bagitFile = new File(rootDir, "bagit.txt");
    Bag bag = readBagitTextFile(bagitFile, new Bag());
    bag.setRootDir(rootDir);
    
    bag = readAllManifests(rootDir, bag);
    
    File bagInfoFile = new File(rootDir, "bag-info.txt");
    if(bagInfoFile.exists()){
      bag = readBagInfo(bagInfoFile, bag);
    }
    
    File fetchFile = new File(rootDir, "fetch.txt");
    if(fetchFile.exists()){
      bag = readFetch(fetchFile, bag);
    }
    
    return bag;
  }
  
  protected static Bag readBagitTextFile(File bagitFile, Bag bag) throws IOException{
    LinkedHashMap<String, String> map = readKeyValueMapFromFile(bagitFile, ":");
    
    Bag newBag = new Bag(bag);
    newBag.setVersion(map.get("BagIt-Version"));
    newBag.setFileEncoding(map.get("Tag-File-Character-Encoding"));
    
    return newBag;
  }
  
  protected static Bag readAllManifests(File rootDir, Bag bag) throws IOException{
    Bag newBag = new Bag(bag);
    File[] files = rootDir.listFiles();
    if(files != null){
      for(File file : files){
        if(file.getName().matches("tagmanifest\\-.*\\.txt")){
          newBag.getTagManifests().add(readManifest(file));
        }
        else if(file.getName().matches("manifest\\-.*\\.txt")){
          newBag.getPayLoadManifests().add(readManifest(file));
        }
      }
    }
    
    return newBag;
  }
  
  public static Manifest readManifest(File manifestFile) throws IOException{
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
      map.put(file, parts[0]);
      line = br.readLine();
    }
    
    return map;
  }
  
  protected static Bag readBagInfo(File bagInfoFile, Bag bag) throws IOException{
    Bag newBag = new Bag(bag);
    
    LinkedHashMap<String, String> metadata = readKeyValueMapFromFile(bagInfoFile, ":");
    newBag.setMetadata(metadata);
    
    return newBag;
  }
  
  protected static Bag readFetch(File fetchFile, Bag bag) throws IOException{
    Bag newBag = new Bag(bag);
    BufferedReader br = Files.newBufferedReader(Paths.get(fetchFile.toURI()));

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split("\\s+", 3);
      long length = parts[1].equals("-") ? -1 : Long.decode(parts[1]);
      URL url = new URL(parts[0]);
      
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
        map.merge(lastEnteredKey, System.lineSeparator() + line, String::concat);
      }
      else{
        String[] parts = line.split(splitRegex);
        lastEnteredKey = parts[0].trim();
        map.put(lastEnteredKey, parts[1].trim());
      }
       
      line = br.readLine();
    }
    
    return map;
  }
}
