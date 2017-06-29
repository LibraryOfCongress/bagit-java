package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.annotation.Incubating;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Metadata;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.util.PathUtils;
import gov.loc.repository.bagit.writer.BagitFileWriter;
import gov.loc.repository.bagit.writer.ManifestWriter;
import gov.loc.repository.bagit.writer.MetadataWriter;

/**
 * Responsible for creating a bag in place.
 */
//TODO look at cleaning up this class so we don't have to ignore CPD
public final class BagCreator {
  private static final Logger logger = LoggerFactory.getLogger(BagCreator.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private static final String DATE_FORMAT = "yyyy-MM-dd";
  
  private BagCreator(){}
  
  /**
   * Creates a bag in place for version 0.97.
   * This method moves and creates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithms an collection of {@link SupportedAlgorithm} implementations
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * @param metadata the metadata to include when creating the bag. Payload-Oxum and Bagging-Date will be overwritten 
   * 
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing or moving file(s)
   * 
   * @return a {@link Bag} object representing the newly created bagit bag
   */
  @SuppressWarnings("CPD-START")
  public static Bag bagInPlace(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden, final Metadata metadata) throws NoSuchAlgorithmException, IOException{
    final Bag bag = new Bag(new Version(0, 97));
    bag.setRootDir(root);
    logger.info(messages.getString("creating_bag"), bag.getVersion(), root);
    
    final Path dataDir = root.resolve("data");
    moveFilesToDataDirectory(root, dataDir, includeHidden);
    
    BagitFileWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), root);
    
    createManifests(root, dataDir, bag, algorithms, includeHidden);
    
    createMetadataFile(root, dataDir, bag, metadata);
    
    return bag;
  }
  
  /**
   * Creates a bag in place for version 0.97.
   * This method moves and creates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithms an collection of {@link SupportedAlgorithm} implementations
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * 
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing or moving file(s)
   * 
   * @return a {@link Bag} object representing the newly created bagit bag
   */
  public static Bag bagInPlace(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    return bagInPlace(root, algorithms, includeHidden, new Metadata());
  }
  
  private static void moveFilesToDataDirectory(final Path root, final Path dataDir, final boolean includeHidden) throws IOException{
    Files.createDirectory(dataDir);
    try(final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)){
      for(final Path path : directoryStream){
        if(!path.equals(dataDir) && (!Files.isHidden(path) || includeHidden)){
          Files.move(path, dataDir.resolve(path.getFileName()));
        }
      }
    }
  }
  
  private static void createManifests(final Path root, final Path dataDir, final Bag bag, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws IOException, NoSuchAlgorithmException{
    logger.info(messages.getString("creating_payload_manifests"));
    final Map<Manifest, MessageDigest> payloadFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreatePayloadManifestsVistor payloadVisitor = new CreatePayloadManifestsVistor(payloadFilesMap, includeHidden);
    Files.walkFileTree(dataDir, payloadVisitor);
    
    bag.getPayLoadManifests().addAll(payloadFilesMap.keySet());
    ManifestWriter.writePayloadManifests(bag.getPayLoadManifests(), root, root, bag.getFileEncoding());
    
    logger.info(messages.getString("creating_tag_manifests"));
    final Map<Manifest, MessageDigest> tagFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreateTagManifestsVistor tagVistor = new CreateTagManifestsVistor(tagFilesMap, includeHidden);
    Files.walkFileTree(root, tagVistor);
    
    bag.getTagManifests().addAll(tagFilesMap.keySet());
    ManifestWriter.writeTagManifests(bag.getTagManifests(), root, root, bag.getFileEncoding());
  }
  
  private static void createMetadataFile(final Path root, final Path dataDir, final Bag bag, final Metadata metadata) throws IOException{
    bag.setMetadata(metadata);
    
    logger.debug(messages.getString("calculating_payload_oxum"), dataDir);
    final String payloadOxum = PathUtils.generatePayloadOxum(PathUtils.getDataDir(bag.getVersion(), root));
    bag.getMetadata().upsertPayloadOxum(payloadOxum);
    
    bag.getMetadata().remove("Bagging-Date");
    bag.getMetadata().add("Bagging-Date", new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).format(new Date()));
    
    logger.info(messages.getString("creating_metadata_file"));
    MetadataWriter.writeBagMetadata(bag.getMetadata(), bag.getVersion(), root, bag.getFileEncoding());
  }
  
  /**
   * Creates a basic(only required elements) .bagit bag in place.
   * This creates files and directories, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithms an collection of {@link SupportedAlgorithm} implementations
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * 
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing files or .bagit directory
   * 
   * @return a {@link Bag} object representing the newly created bagit bag
   */
  @Incubating
  @SuppressWarnings("CPD-END")
  public static Bag createDotBagit(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    final Bag bag = new Bag(new Version(2, 0));
    bag.setRootDir(root);
    logger.info(messages.getString("creating_bag"), bag.getVersion(), root);
    
    final Path dotbagitDir = root.resolve(".bagit");
    Files.createDirectories(dotbagitDir);
    
    logger.info(messages.getString("creating_payload_manifests"));
    final Map<Manifest, MessageDigest> map = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreatePayloadManifestsVistor visitor = new CreatePayloadManifestsVistor(map, includeHidden);
    Files.walkFileTree(root, visitor);
    
    bag.getPayLoadManifests().addAll(map.keySet());
    BagitFileWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), dotbagitDir);
    ManifestWriter.writePayloadManifests(bag.getPayLoadManifests(), dotbagitDir, root, bag.getFileEncoding());
    
    logger.info(messages.getString("creating_tag_manifests"));
    final Map<Manifest, MessageDigest> tagFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreateTagManifestsVistor tagVistor = new CreateTagManifestsVistor(tagFilesMap, includeHidden);
    Files.walkFileTree(dotbagitDir, tagVistor);
    
    bag.getTagManifests().addAll(tagFilesMap.keySet());
    ManifestWriter.writeTagManifests(bag.getTagManifests(), dotbagitDir, root, bag.getFileEncoding());
    
    return bag;
  }
}
