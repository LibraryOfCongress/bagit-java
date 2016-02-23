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
import gov.loc.repository.bagit.verify.Verifier;

/**
 * responsible for writing out a bag.
 */
public class BagWriter {
  private static final Logger logger = LoggerFactory.getLogger(Verifier.class);

  /**
   * Write the bag out to the specified directory. 
   * If an error occurs some of the files may have been written out to the filesystem.
   * @param bag the {@link Bag} object to write out
   * @param outputDir the output directory that will become the root of the bag
   * @throws IOException if there is a problem writing a file
   */
  public static void write(Bag bag, File outputDir) throws IOException{
    writeBagitFile(bag.getVersion(), bag.getFileEncoding(), outputDir);
    writePayloadManifests(bag.getPayLoadManifests(), outputDir, bag.getFileEncoding());
    writePayloadFiles(bag.getPayLoadManifests(), outputDir);

    if(bag.getTagManifests().size() > 0){
      writeTagManifests(bag.getTagManifests(), outputDir, bag.getFileEncoding());
    }
    if(bag.getMetadata().size() > 0){
      writeBagitInfoFile(bag.getMetadata(), outputDir, bag.getFileEncoding());
    }
    if(bag.getItemsToFetch().size() > 0){
      writeFetchFile(bag.getItemsToFetch(), outputDir, bag.getFileEncoding());
    }
  }
  
  /**
   * Write the bagit.txt file in required UTF-8 encoding.
   * @param version the version of the bag to write out
   * @param encoding the encoding of the tag files
   * @param outputDir the root of the bag
   * @throws IOException if there was a problem writing the file
   */
  public static void writeBagitFile(String version, String encoding, File outputDir) throws IOException{
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
   * @param outputDir the root of the bag
   * @throws IOException if there was a problem writing a file
   */
  public static void writePayloadFiles(Set<Manifest> payloadManifests, File outputDir) throws IOException{
    for(Manifest payloadManifest : payloadManifests){
      for(File payloadFile : payloadManifest.getFileToChecksumMap().keySet()){
        Path writeToPath = Paths.get(outputDir.getPath(), getPathRelativeToDataDir(payloadFile));
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
    writeManifests(tagManifests, outputDir, "tagmanifest-", charsetName);
  }
  
  protected static void writeManifests(Set<Manifest> tagManifests, File outputDir, String filenameBase, String charsetName) throws IOException{
    for(Manifest manifest : tagManifests){
      Path manifestPath = Paths.get(outputDir.getPath(), filenameBase + manifest.getAlgorithm().toLowerCase() + ".txt");
      logger.debug("Writing manifest to [{}]", manifestPath);
      
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
    String path = file.getPath();
    int index = path.indexOf("data");
    
    if(index == -1){
      return file.getName();
    }
    
    return path.substring(index, path.length());
  }
}
