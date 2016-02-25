package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.verify.BagVerifier;

/**
 * responsible for writing out a bag.
 */
public class BagWriter {
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);

  /**
   * Write the bag out to the specified directory. 
   * If an error occurs some of the files may have been written out to the filesystem.
   * @param bag the {@link Bag} object to write out
   * @param outputDir the output directory that will become the root of the bag
   * @throws IOException if there is a problem writing a file
   */
  public static void write(Bag bag, File outputDir) throws IOException{
    File bagitDir = writeVersionDependentPayloadFiles(bag, outputDir);
    
    writeBagitFile(bag.getVersion(), bag.getFileEncoding(), bagitDir);
    writePayloadManifests(bag.getPayLoadManifests(), bagitDir, bag.getFileEncoding());
    
    
    if(bag.getTagManifests().size() > 0){
      writeTagManifests(bag.getTagManifests(), bagitDir, bag.getFileEncoding());
    }
    if(bag.getMetadata().size() > 0){
      writeBagitInfoFile(bag.getMetadata(), bagitDir, bag.getFileEncoding());
    }
    if(bag.getItemsToFetch().size() > 0){
      writeFetchFile(bag.getItemsToFetch(), bagitDir, bag.getFileEncoding());
    }
  }
  
  protected static File writeVersionDependentPayloadFiles(Bag bag, File outputDir) throws IOException{
    File bagitDir = outputDir;
    if(bag.getVersion().compareTo(new Version(0, 98)) >= 0){
      bagitDir = new File(outputDir, ".bagit");
      if(!bagitDir.mkdirs()){
        throw new IOException("Could not make directory " + bagitDir);
      }
      writePayloadFiles(bag.getPayLoadManifests(), outputDir, bag.getRootDir());
    }
    else{
      File dataDir = new File(outputDir, "data");
      writePayloadFiles(bag.getPayLoadManifests(), dataDir, bag.getRootDir());
    }
    
    return bagitDir;
  }
  
  /**
   * Write the bagit.txt file in required UTF-8 encoding.
   * @param version the version of the bag to write out
   * @param encoding the encoding of the tag files
   * @param outputDir the root of the bag
   * @throws IOException if there was a problem writing the file
   */
  public static void writeBagitFile(Version version, String encoding, File outputDir) throws IOException{
    logger.debug("Writing bagit.txt file to [{}]", outputDir);
    Path bagitPath = Paths.get(outputDir.getPath(), "bagit.txt");
    
    String firstLine = "BagIt-Version : " + version + System.lineSeparator();
    logger.debug("Writing line [{}] to [{}]", firstLine, bagitPath);
    Files.write(bagitPath, firstLine.getBytes(StandardCharsets.UTF_8), 
        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    
    String secondLine = "Tag-File-Character-Encoding : " + encoding + System.lineSeparator();
    logger.debug("Writing line [{}] to [{}]", secondLine, bagitPath);
    Files.write(bagitPath, secondLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
  }
  
  /**
   * Write the payload <b>file(s)</b> to the output directory
   * @param payloadManifests the set of objects representing the payload manifests
   * @param outputDir the data directory of the bag
   * @param bagRootDir the root directory of the bag
   * @throws IOException if there was a problem writing a file
   */
  public static void writePayloadFiles(Set<Manifest> payloadManifests, File outputDir, File bagRootDir) throws IOException{
    logger.info("Writing payload files");
    for(Manifest payloadManifest : payloadManifests){
      for(File payloadFile : payloadManifest.getFileToChecksumMap().keySet()){
        Path bagRootPath = Paths.get(bagRootDir.toURI());
        Path payloadPath = Paths.get(payloadFile.toURI());
        Path relativePayloadPath = bagRootPath.relativize(payloadPath); 
            
        Path writeToPath = Paths.get(outputDir.getPath(), relativePayloadPath.toString());
        logger.debug("Writing payload file [{}] to [{}]", payloadFile, writeToPath);
        Path parent = writeToPath.getParent();
        if(parent != null){
          Files.createDirectories(parent);
        }
        Files.copy(Paths.get(payloadFile.toURI()), writeToPath, 
            StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }
  
  /**
   * Write the payload <b>manifest(s)</b> to the output directory
   * @param manifests the payload{@link Manifest}s to write out
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * @throws IOException if there was a problem writing a file
   */
  public static void writePayloadManifests(Set<Manifest> manifests, File outputDir, String charsetName) throws IOException{
    logger.info("Writing payload manifest(s)");
    writeManifests(manifests, outputDir, "manifest-", charsetName);
  }
  
  /**
   * Write the tag <b>manifest(s)</b> to the output directory
   * @param tagManifests the tag{@link Manifest}s to write out
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * @throws IOException if there was a problem writing a file
   */
  public static void writeTagManifests(Set<Manifest> tagManifests, File outputDir, String charsetName) throws IOException{
    logger.info("Writing tag manifest(s)");
    writeManifests(tagManifests, outputDir, "tagmanifest-", charsetName);
  }
  
  protected static void writeManifests(Set<Manifest> manifests, File outputDir, String filenameBase, String charsetName) throws IOException{
    for(Manifest manifest : manifests){
      Path manifestPath = Paths.get(outputDir.getPath(), filenameBase + manifest.getAlgorithm().getBagitName() + ".txt");
      logger.debug("Writing manifest to [{}]", manifestPath);
      
      Files.createFile(manifestPath);
      
      for(Entry<File, String> entry : manifest.getFileToChecksumMap().entrySet()){
        String line = entry.getValue() + " " + getPathRelativeToDataDir(entry.getKey()) + System.lineSeparator();
        logger.debug("Writing [{}] to [{}]", line, manifestPath);
        Files.write(manifestPath, line.getBytes(Charset.forName(charsetName)), 
            StandardOpenOption.APPEND, StandardOpenOption.CREATE);
      }
    }
  }
  
  /**
   * Write the bag-info.txt file to the specified outputDir with specified encoding (charsetName)
   * @param metadata the key value pair info in the bag-info.txt file
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * @throws IOException if there was a problem writing a file
   */
  public static void writeBagitInfoFile(LinkedHashMap<String, String> metadata, File outputDir, String charsetName) throws IOException{
    logger.debug("Writing bag-info.txt to [{}]", outputDir);
    Path outputPath = Paths.get(outputDir.getPath(), "bag-info.txt");
    
    for(Entry<String, String> entry : metadata.entrySet()){
      String line = entry.getKey() + " : " + entry.getValue() + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, outputPath);
      Files.write(outputPath, line.getBytes(Charset.forName(charsetName)), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  /**
   * Write the fetch.txt file to the outputDir with the specified encoding (charsetName)
   * @param itemsToFetch the list of {@link FetchItem}s to write into the fetch.txt
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * @throws IOException if there was a problem writing a file
   */
  public static void writeFetchFile(List<FetchItem> itemsToFetch, File outputDir, String charsetName) throws IOException{
    logger.debug("Writing fetch.txt to [{}]", outputDir);
    Path outputPath = Paths.get(outputDir.getPath(), "fetch.txt");
    
    for(FetchItem item : itemsToFetch){
      String line = item.toString();
      logger.debug("Writing [{}] to [{}]", line, outputPath);
      Files.write(outputPath, line.getBytes(Charset.forName(charsetName)), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  protected static String getPathRelativeToDataDir(File file){
    logger.debug("getting path relative to data directory for [{}]", file);
    String path = file.getPath();
    int index = path.indexOf("data");
    
    if(index == -1){
      return file.getName();
    }
    
    String rel = path.substring(index, path.length());
    logger.debug("Relative path for file [{}] to data directory is [{}]", file, rel);
    
    return rel;
  }
}
