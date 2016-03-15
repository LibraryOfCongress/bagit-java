package gov.loc.repository.bagit.operations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
import javafx.util.Pair;

/**
 * responsible for writing out a bag.
 */
public class BagWriter {
  private static final Logger logger = LoggerFactory.getLogger(BagVerifier.class);

  private BagWriter(){}
  
  /**
   * Write the bag out to the specified directory. 
   * If an error occurs some of the files may have been written out to the filesystem.
   * @param bag the {@link Bag} object to write out
   * @param outputDir the output directory that will become the root of the bag
   * @throws IOException if there is a problem writing a file
   */
  public static void write(Bag bag, Path outputDir) throws IOException{
    Path bagitDir = writeVersionDependentPayloadFiles(bag, outputDir);
    
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
  
  protected static Path writeVersionDependentPayloadFiles(Bag bag, Path outputDir) throws IOException{
    Path bagitDir = outputDir;
    //@Incubating
    if(bag.getVersion().compareTo(new Version(0, 98)) >= 0){
      bagitDir = outputDir.resolve(".bagit");
      Files.createDirectories(bagitDir);
      writePayloadFiles(bag.getPayLoadManifests(), outputDir, bag.getRootDir());
    }
    else{
      Path dataDir = outputDir.resolve("data");
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
  public static void writeBagitFile(Version version, String encoding, Path outputDir) throws IOException{
    logger.debug("Writing bagit.txt file to [{}]", outputDir);
    Path bagitPath = outputDir.resolve("bagit.txt");
    
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
  public static void writePayloadFiles(Set<Manifest> payloadManifests, Path outputDir, Path bagRootDir) throws IOException{
    logger.info("Writing payload files");
    for(Manifest payloadManifest : payloadManifests){
      for(Path payloadFile : payloadManifest.getFileToChecksumMap().keySet()){
        Path relativePayloadPath = bagRootDir.relativize(payloadFile); 
            
        Path writeToPath = outputDir.resolve(relativePayloadPath);
        logger.debug("Writing payload file [{}] to [{}]", payloadFile, writeToPath);
        Path parent = writeToPath.getParent();
        if(parent != null){
          Files.createDirectories(parent);
        }
        Files.copy(payloadFile, writeToPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
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
  public static void writePayloadManifests(Set<Manifest> manifests, Path outputDir, String charsetName) throws IOException{
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
  public static void writeTagManifests(Set<Manifest> tagManifests, Path outputDir, String charsetName) throws IOException{
    logger.info("Writing tag manifest(s)");
    writeManifests(tagManifests, outputDir, "tagmanifest-", charsetName);
  }
  
  protected static void writeManifests(Set<Manifest> manifests, Path outputDir, String filenameBase, String charsetName) throws IOException{
    for(Manifest manifest : manifests){
      writeManifest(manifest, outputDir, filenameBase, charsetName);
    }
  }
  
  protected static void writeManifest(Manifest manifest, Path outputDir, String filenameBase, String charsetName) throws IOException{
    Path manifestPath = outputDir.resolve(filenameBase + manifest.getAlgorithm().getBagitName() + ".txt");
    logger.debug("Writing manifest to [{}]", manifestPath);
    
    Files.createFile(manifestPath);
    
    for(Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
      String line = entry.getValue() + " " + getPathRelativeToDataDir(entry.getKey()) + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, manifestPath);
      Files.write(manifestPath, line.getBytes(Charset.forName(charsetName)), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  /**
   * Write the bag-info.txt file to the specified outputDir with specified encoding (charsetName)
   * @param metadata the key value pair info in the bag-info.txt file
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * @throws IOException if there was a problem writing a file
   */
  public static void writeBagitInfoFile(List<Pair<String, String>> metadata, Path outputDir, String charsetName) throws IOException{
    logger.debug("Writing bag-info.txt to [{}]", outputDir);
    Path bagInfoFilePath = outputDir.resolve("bag-info.txt");
    
    for(Pair<String, String> entry : metadata){
      String line = entry.getKey() + " : " + entry.getValue() + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, bagInfoFilePath);
      Files.write(bagInfoFilePath, line.getBytes(Charset.forName(charsetName)), 
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
  public static void writeFetchFile(List<FetchItem> itemsToFetch, Path outputDir, String charsetName) throws IOException{
    logger.debug("Writing fetch.txt to [{}]", outputDir);
    Path fetchFilePath = outputDir.resolve("fetch.txt");
    
    for(FetchItem item : itemsToFetch){
      String line = item.toString();
      logger.debug("Writing [{}] to [{}]", line, fetchFilePath);
      Files.write(fetchFilePath, line.getBytes(Charset.forName(charsetName)), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  protected static String getPathRelativeToDataDir(Path file){
    logger.debug("getting path relative to data directory for [{}]", file);
    String path = file.toString();
    int index = path.indexOf("data");
    
    if(index == -1){
      return file.toString();
    }
    
    String rel = path.substring(index, path.length());
    logger.debug("Relative path for file [{}] to data directory is [{}]", file, rel);
    
    return rel;
  }
}
