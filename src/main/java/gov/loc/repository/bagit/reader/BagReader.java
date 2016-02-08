package gov.loc.repository.bagit.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;

/**
 * responsible for reading a bag from the filesystem.
 */
public class BagReader {
  /**
   * Read the bag from the filesystem and create a bag object 
   * @throws IOException 
   */
  //TODO read in parallel
  public static Bag read(File rootDir) throws IOException{
    File bagitFile = new File(rootDir, "bagit.txt");
    Bag bag = readBagitTextFile(bagitFile);
    
    bag = readAllManifests(rootDir, bag);
    
    File bagInfoFile = new File(rootDir, "bag-info.txt");
    bag = readBagInfo(bagInfoFile, bag);
    
    File fetchFile = new File(rootDir, "fetch.txt");
    bag = readFetch(fetchFile, bag);
    
    return bag;
  }
  
  protected static Bag readBagitTextFile(File bagitFile) throws IOException{
    LinkedHashMap<String, String> map = readKeyValueMapFromFile(bagitFile, ":");
    
    Bag bag = new Bag(map.get("BagIt-Version"));
    bag.setFileEncoding(map.get("Tag-File-Character-Encoding"));
    
    return bag;
  }
  
  protected static Bag readAllManifests(File rootDir, Bag bag) throws IOException{
    Bag newBag = new Bag(bag);
    //TODO
    File[] files = rootDir.listFiles();
    if(files != null){
      for(File file : files){
        if(file.getName().matches("tagmanifest\\-.*\\.txt")){
          newBag.getTagManifests().add(readTagManifest(rootDir, file));
        }
        else if(file.getName().matches("manifest\\-.*\\.txt")){
          
        }
      }
    }
    
    return newBag;
  }
  
  protected static Manifest readTagManifest(File rootDir, File manifestFile) throws IOException{
    String alg = manifestFile.getName().split("[-\\.]")[1];
    Manifest manifest = new Manifest(alg);
    
    HashMap<File, String> filetToChecksumMap = readChecksumFileMap(rootDir, manifestFile);
    manifest.setFileToChecksumMap(filetToChecksumMap);
    
    return manifest;
  }
  
  protected static Bag readBagInfo(File bagInfoFile, Bag bag){
    Bag newBag = new Bag(bag);
    //TODO
    //read bag-info.txt
    return newBag;
  }
  
  protected static Bag readFetch(File fetchFile, Bag bag){
    Bag newBag = new Bag(bag);
    //TODO
    //read fetch.txt
    return newBag;
  }
  
  protected static HashMap<File, String> readChecksumFileMap(File rootDir, File manifestFile) throws IOException{
    HashMap<File, String> map = new HashMap<>();
    BufferedReader br = Files.newBufferedReader(Paths.get(manifestFile.toURI()));

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split("\\s+");
      File file = new File(rootDir, parts[1]);
      map.put(file, parts[0]);
      line = br.readLine();
    }
    
    return map;
  }
  
  protected static LinkedHashMap<String, String> readKeyValueMapFromFile(File file, String splitRegex) throws IOException{
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    BufferedReader br = Files.newBufferedReader(Paths.get(file.toURI()));

    String line = br.readLine();
    while(line != null){
      String[] parts = line.split(splitRegex);
      map.put(parts[0].trim(), parts[1].trim());
      line = br.readLine();
    }
    
    return map;
  }
}
