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
   * Write the bag out to the specified directory
   * @throws IOException 
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
  
  protected static void writeBagitFile(String version, String encoding, File outputDir) throws IOException{
    logger.debug("Writing bagit.txt file to [{}]", outputDir);
    Path bagitPath = Paths.get(outputDir.getPath(), "bagit.txt");
    
    String firstLine = "BagIt-Version : " + version + System.lineSeparator();
    logger.debug("Writing line [{}] to [{}]", firstLine, bagitPath);
    Files.write(bagitPath, firstLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    
    String secondLine = "Tag-File-Character-Encoding : " + encoding + System.lineSeparator();
    logger.debug("Writing line [{}] to [{}]", secondLine, bagitPath);
    Files.write(bagitPath, secondLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
  }
  
  //copy the payload files to the destination /data directory and make needed parent directories
  protected static void writePayloadFiles(Set<Manifest> payloadManifests, File outputDir) throws IOException{
    for(Manifest payloadManifest : payloadManifests){
      for(File payloadFile : payloadManifest.getFileToChecksumMap().keySet()){
        Path writeToPath = Paths.get(outputDir.getPath(), getPathRelativeToDataDir(payloadFile));
        logger.debug("Writing payload file [{}] to [{}]", payloadFile, writeToPath);
        Files.createDirectories(writeToPath.getParent());
        Files.copy(Paths.get(payloadFile.toURI()), writeToPath, 
            StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }
  
  //write the manifests out
  protected static void writePayloadManifests(Set<Manifest> manifests, File outputDir, String charsetName) throws IOException{
    writeManifests(manifests, outputDir, "manifest-", charsetName);
  }
  
  //write the tag manifests
  protected static void writeTagManifests(Set<Manifest> tagManifests, File outputDir, String charsetName) throws IOException{
    writeManifests(tagManifests, outputDir, "tagmanifest-", charsetName);
  }
  
  protected static void writeManifests(Set<Manifest> tagManifests, File outputDir, String filenameBase, String charsetName) throws IOException{
    for(Manifest manifest : tagManifests){
      Path manifestPath = Paths.get(outputDir.getPath(), filenameBase + manifest.getAlgorithm() + ".txt");
      logger.debug("Writing manifest to [{}]", manifestPath);
      
      for(Entry<File, String> entry : manifest.getFileToChecksumMap().entrySet()){
        String line = entry.getValue() + " " + getPathRelativeToDataDir(entry.getKey()) + System.lineSeparator();
        logger.debug("Writing [{}] to [{}]", line, manifestPath);
        Files.write(manifestPath, line.getBytes(Charset.forName(charsetName)), StandardOpenOption.APPEND);
      }
    }
  }
  
  protected static void writeBagitInfoFile(LinkedHashMap<String, String> metadata, File outputDir, String charsetName) throws IOException{
    logger.debug("Writing bagit-info.txt to [{}]", outputDir);
    Path outputPath = Paths.get(outputDir.getPath(), "bag-info.txt");
    
    for(Entry<String, String> entry : metadata.entrySet()){
      String line = entry.getKey() + " : " + entry.getValue() + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, outputPath);
      Files.write(outputPath, line.getBytes(Charset.forName(charsetName)), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  protected static void writeFetchFile(List<FetchItem> itemsToFetch, File outputDir, String charsetName) throws IOException{
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
    
    return path.substring(index, path.length());
  }
}
