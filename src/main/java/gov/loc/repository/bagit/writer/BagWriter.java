package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.util.PathUtils;
import javafx.util.Pair;

/**
 * responsible for writing out a bag.
 */
@SuppressWarnings(value = {"PMD.TooManyMethods", "PMD.AvoidInstantiatingObjectsInLoops"}) //TODO refactor to remove methods?
public final class BagWriter {
  private static final Logger logger = LoggerFactory.getLogger(BagWriter.class);
  private static final Version VERSION_0_98 = new Version(0, 98);
  private static final Version VERSION_0_95 = new Version(0, 95);

  private BagWriter(){
    //intentionally left empty
  }
  
  /**
   * Write the bag out to the specified directory. 
   * If an error occurs some of the files may have been written out to the filesystem.
   * tag manifest(s) are updated prior to writing to ensure bag is valid after completion, 
   * it is therefore recommended if you are going to further interact with the bag to read it from specified outputDir path
   * 
   * @param bag the {@link Bag} object to write out
   * @param outputDir the output directory that will become the root of the bag
   * 
   * @throws IOException if there is a problem writing a file
   * @throws NoSuchAlgorithmException when trying to generate a {@link MessageDigest} which is used during update.
   */
  public static void write(final Bag bag, final Path outputDir) throws IOException, NoSuchAlgorithmException{
    final Path bagitDir = writeVersionDependentPayloadFiles(bag, outputDir);
    
    writeBagitFile(bag.getVersion(), bag.getFileEncoding(), bagitDir);
    writePayloadManifests(bag.getPayLoadManifests(), bagitDir, bag.getRootDir(), bag.getFileEncoding());

    if(bag.getMetadata().size() > 0){
      writeBagitInfoFile(bag.getMetadata(), bag.getVersion(), bagitDir, bag.getFileEncoding());
    }
    if(bag.getItemsToFetch().size() > 0){
      writeFetchFile(bag.getItemsToFetch(), bagitDir, bag.getFileEncoding());
    }
    if(bag.getTagManifests().size() > 0){
      writeAdditionalTagPayloadFiles(bag.getTagManifests(), bagitDir, bag.getRootDir());
      final Set<Manifest> updatedTagManifests = updateTagManifests(bag, outputDir);
      bag.setTagManifests(updatedTagManifests);
      writeTagManifests(updatedTagManifests, bagitDir, outputDir, bag.getFileEncoding());
    }
  }
  
  private static Path writeVersionDependentPayloadFiles(final Bag bag, final Path outputDir) throws IOException{
    Path bagitDir = outputDir;
    //@Incubating
    if(VERSION_0_98.compareTo(bag.getVersion()) <= 0){
      bagitDir = outputDir.resolve(".bagit");
      Files.createDirectories(bagitDir);
      writePayloadFiles(bag.getPayLoadManifests(), outputDir, bag.getRootDir());
    }
    else{
      final Path dataDir = outputDir.resolve("data");
      Files.createDirectories(dataDir);
      writePayloadFiles(bag.getPayLoadManifests(), dataDir, bag.getRootDir().resolve("data"));
    }
    
    return bagitDir;
  }
  
  /**
   * Write the bagit.txt file in required UTF-8 encoding.
   * 
   * @param version the version of the bag to write out
   * @param encoding the encoding of the tag files
   * @param outputDir the root of the bag
   * 
   * @throws IOException if there was a problem writing the file
   */
  public static void writeBagitFile(final Version version, final Charset encoding, final Path outputDir) throws IOException{
    final Path bagitPath = outputDir.resolve("bagit.txt");
    logger.debug("Writing bagit.txt file to [{}]", outputDir);
    
    
    final String firstLine = "BagIt-Version : " + version + System.lineSeparator();
    logger.debug("Writing line [{}] to [{}]", firstLine, bagitPath);
    Files.write(bagitPath, firstLine.getBytes(StandardCharsets.UTF_8), 
        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    
    final String secondLine = "Tag-File-Character-Encoding : " + encoding + System.lineSeparator();
    logger.debug("Writing line [{}] to [{}]", secondLine, bagitPath);
    Files.write(bagitPath, secondLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
  }
  
  /**
   * Write the payload <b>file(s)</b> to the output directory
   * 
   * @param payloadManifests the set of objects representing the payload manifests
   * @param outputDir the data directory of the bag
   * @param bagDataDir the data directory of the bag
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writePayloadFiles(final Set<Manifest> payloadManifests, final Path outputDir, final Path bagDataDir) throws IOException{
    logger.info("Writing payload files");
    for(final Manifest payloadManifest : payloadManifests){
      for(final Path payloadFile : payloadManifest.getFileToChecksumMap().keySet()){
        final Path relativePayloadPath = bagDataDir.relativize(payloadFile); 
            
        final Path writeToPath = outputDir.resolve(relativePayloadPath);
        logger.debug("Writing payload file [{}] to [{}]", payloadFile, writeToPath);
        final Path parent = writeToPath.getParent();
        if(parent != null){
          Files.createDirectories(parent);
        }
        Files.copy(payloadFile, writeToPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }
  
  /**
   * Write the payload <b>manifest(s)</b> to the output directory
   * 
   * @param manifests the payload{@link Manifest}s to write out
   * @param outputDir the root of where the manifest is being written to
   * @param bagitRootDir the path to the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writePayloadManifests(final Set<Manifest> manifests, final Path outputDir, final Path bagitRootDir, final Charset charsetName) throws IOException{
    logger.info("Writing payload manifest(s)");
    writeManifests(manifests, outputDir, bagitRootDir, "manifest-", charsetName);
  }
  
  private static Set<Manifest> updateTagManifests(final Bag bag, final Path newBagRootDir) throws NoSuchAlgorithmException, IOException{
    final Set<Manifest> newManifests = new HashSet<>();
    
    for(final Manifest tagManifest : bag.getTagManifests()){
      final Manifest newManifest = new Manifest(tagManifest.getAlgorithm());
      
      for(final Path originalPath : tagManifest.getFileToChecksumMap().keySet()){
        final Path relativePath = bag.getRootDir().relativize(originalPath);
        final Path pathToUpdate = newBagRootDir.resolve(relativePath);
        final MessageDigest messageDigest = MessageDigest.getInstance(tagManifest.getAlgorithm().getMessageDigestName());
        final String newChecksum = Hasher.hash(pathToUpdate, messageDigest);
        newManifest.getFileToChecksumMap().put(pathToUpdate, newChecksum);
      }
      
      newManifests.add(newManifest);
    }
    
    return newManifests;
  }
  
  /**
   * Write the tag <b>manifest(s)</b> to the output directory
   * 
   * @param tagManifests the tag{@link Manifest}s to write out
   * @param outputDir the root of where the manifest is being written to
   * @param bagitRootDir the path to the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writeTagManifests(final Set<Manifest> tagManifests, final Path outputDir, final Path bagitRootDir, final Charset charsetName) throws IOException{
    logger.info("Writing tag manifest(s)");
    writeManifests(tagManifests, outputDir, bagitRootDir, "tagmanifest-", charsetName);
  }
  
  private static void writeManifests(final Set<Manifest> manifests, final Path outputDir, final Path relativeTo, final String filenameBase, final Charset charsetName) throws IOException{
    for(final Manifest manifest : manifests){
      final Path manifestPath = outputDir.resolve(filenameBase + manifest.getAlgorithm().getBagitName() + ".txt");
      logger.debug("Writing manifest to [{}]", manifestPath);

      Files.deleteIfExists(manifestPath);
      Files.createFile(manifestPath);
      
      for(final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
        final String line = entry.getValue() + " " + 
            PathUtils.encodeFilename(relativeTo.relativize(entry.getKey())) + System.lineSeparator();
        logger.debug("Writing [{}] to [{}]", line, manifestPath);
        Files.write(manifestPath, line.getBytes(charsetName), 
            StandardOpenOption.APPEND, StandardOpenOption.CREATE);
      }
    }
  }
  
  private static void writeAdditionalTagPayloadFiles(final Set<Manifest> manifests, final Path outputDir, final Path bagRootDir) throws IOException{
    for(final Manifest manifest : manifests){
      for(final Entry<Path, String> entry : manifest.getFileToChecksumMap().entrySet()){
        final Path relativeLocation = bagRootDir.relativize(entry.getKey());
        final Path writeTo = outputDir.resolve(relativeLocation);
        final Path writeToParent = writeTo.getParent();
        if(!Files.exists(writeTo) && writeToParent != null){
          Files.createDirectories(writeToParent);
          Files.copy(entry.getKey(), writeTo);
        }
      }
    }
  }
  
  /**
   * Write the bag-info.txt (or package-info.txt) file to the specified outputDir with specified encoding (charsetName)
   * 
   * @param metadata the key value pair info in the bag-info.txt file
   * @param version the version of the bag you are writing
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writeBagitInfoFile(final List<Pair<String, String>> metadata, final Version version, final Path outputDir, final Charset charsetName) throws IOException{
    Path bagInfoFilePath = outputDir.resolve("bag-info.txt");
    if(VERSION_0_95.compareTo(version) >= 0){
      bagInfoFilePath = outputDir.resolve("package-info.txt");
    }
    logger.debug("Writing {} to [{}]", bagInfoFilePath.getFileName(), outputDir);

    Files.deleteIfExists(bagInfoFilePath);
    
    for(final Pair<String, String> entry : metadata){
      final String line = entry.getKey() + " : " + entry.getValue() + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, bagInfoFilePath);
      Files.write(bagInfoFilePath, line.getBytes(charsetName), 
          StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
  
  /**
   * Write the fetch.txt file to the outputDir with the specified encoding (charsetName)
   * 
   * @param itemsToFetch the list of {@link FetchItem}s to write into the fetch.txt
   * @param outputDir the root of the bag
   * @param charsetName the name of the encoding for the file
   * 
   * @throws IOException if there was a problem writing a file
   */
  public static void writeFetchFile(final List<FetchItem> itemsToFetch, final Path outputDir, final Charset charsetName) throws IOException{
    logger.debug("Writing fetch.txt to [{}]", outputDir);
    final Path fetchFilePath = outputDir.resolve("fetch.txt");
    
    for(final FetchItem item : itemsToFetch){
      final String line = item.toString() + System.lineSeparator();
      logger.debug("Writing [{}] to [{}]", line, fetchFilePath);
      Files.write(fetchFilePath, line.getBytes(charsetName), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }
}
