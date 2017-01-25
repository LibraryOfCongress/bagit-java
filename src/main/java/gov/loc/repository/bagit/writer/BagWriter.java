package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.Hasher;

/**
 * responsible for writing out a bag.
 */
public final class BagWriter {
  private static final Logger logger = LoggerFactory.getLogger(BagWriter.class);
  private static final String PAYLOAD_OXUM_KEY = "Payload-Oxum";

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
    logger.debug("writing payload files");
    final Path bagitDir = PayloadWriter.writeVersionDependentPayloadFiles(bag, outputDir);

    logger.debug("Calculating the payload oxum");
    final SimpleImmutableEntry<Long, Long> payloadByteAndFileCount = calculatePayloadByteAndFileCount(bag.getPayLoadManifests());
    updateMetadataWithPayloadInfo(bag, payloadByteAndFileCount.getKey(), payloadByteAndFileCount.getValue());
    
    logger.debug("writing the bagit.txt file");
    BagitFileWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), payloadByteAndFileCount.getKey(), 
        payloadByteAndFileCount.getValue(), bagitDir);
    
    logger.debug("writing the payload manifest(s)");
    ManifestWriter.writePayloadManifests(bag.getPayLoadManifests(), bagitDir, bag.getRootDir(), bag.getFileEncoding());

    if(bag.getMetadata().size() > 0){
      logger.debug("writing the bag metadata");
      MetadataWriter.writeBagMetadata(bag.getMetadata(), bag.getVersion(), bagitDir, bag.getFileEncoding());
    }
    if(bag.getItemsToFetch().size() > 0){
      logger.debug("writing the fetch file");
      FetchWriter.writeFetchFile(bag.getItemsToFetch(), bagitDir, bag.getRootDir(), bag.getFileEncoding());
    }
    if(bag.getTagManifests().size() > 0){
      logger.debug("writing the tag manifest(s)");
      writeTagManifestFiles(bag.getTagManifests(), bagitDir, bag.getRootDir());
      final Set<Manifest> updatedTagManifests = updateTagManifests(bag, outputDir);
      bag.setTagManifests(updatedTagManifests);
      ManifestWriter.writeTagManifests(updatedTagManifests, bagitDir, outputDir, bag.getFileEncoding());
    }
  }
  
  private static SimpleImmutableEntry<Long, Long> calculatePayloadByteAndFileCount(final Set<Manifest> manifests) throws IOException{
    long byteCount = 0l;
    
    final Set<Path> uniquePaths = new HashSet<>();
    for(final Manifest manifest : manifests){
      uniquePaths.addAll(manifest.getFileToChecksumMap().keySet());
    }
    
    for(final Path file : uniquePaths){
      byteCount += Files.size(file);
    }
    
    return new SimpleImmutableEntry<>(byteCount, Long.valueOf(uniquePaths.size()));
  }
  
  private static void updateMetadataWithPayloadInfo(final Bag bag, final long byteCount, final long fileCount){
    final List<SimpleImmutableEntry<String, String>> updatedMetadata = new ArrayList<>();
    
    for(final SimpleImmutableEntry<String, String> metadata : bag.getMetadata()){
      if(!PAYLOAD_OXUM_KEY.equals(metadata.getKey())){
        updatedMetadata.add(metadata);
      }
    }
    
    updatedMetadata.add(new SimpleImmutableEntry<>(PAYLOAD_OXUM_KEY, byteCount + "." + fileCount));
    bag.setMetadata(updatedMetadata);
  }
  
  /*
   * Update the tag manifest cause the checksum of the other tag files will have changed since we just wrote them out to disk
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
  
  /*
   * Write the tag manifest files
   */
  private static void writeTagManifestFiles(final Set<Manifest> manifests, final Path outputDir, final Path bagRootDir) throws IOException{
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
  
  
}
