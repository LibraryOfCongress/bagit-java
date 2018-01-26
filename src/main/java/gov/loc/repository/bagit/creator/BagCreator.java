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
@SuppressWarnings("PMD.TooManyMethods")
public final class BagCreator {
  private static final Logger logger = LoggerFactory.getLogger(BagCreator.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final Version DOT_BAGIT_VERSION = new Version(2, 0);
  private static final Version LATEST_NON_DOT_BAGIT_VERSION = new Version(0, 97);
  
  private BagCreator(){}
  
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
    return bagInPlace(LATEST_NON_DOT_BAGIT_VERSION, root, algorithms, includeHidden, new Metadata());
  }
  
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
  public static Bag bagInPlace(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden, final Metadata metadata) throws NoSuchAlgorithmException, IOException{
    return bagInPlace(LATEST_NON_DOT_BAGIT_VERSION, root, algorithms, includeHidden, metadata);
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
  public static Bag createDotBagit(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    return bagInPlace(DOT_BAGIT_VERSION, root, algorithms, includeHidden, new Metadata());
  }
  
  /**
   * Creates a basic(only required elements) .bagit bag in place.
   * This creates files and directories, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithms an collection of {@link SupportedAlgorithm} implementations
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * @param metadata the metadata to include when creating the bag. Payload-Oxum and Bagging-Date will be overwritten
   * 
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing files or .bagit directory
   * 
   * @return a {@link Bag} object representing the newly created bagit bag
   */
  @Incubating
  public static Bag createDotBagit(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden, final Metadata metadata) throws NoSuchAlgorithmException, IOException{
    return bagInPlace(DOT_BAGIT_VERSION, root, algorithms, includeHidden, metadata);
  }
  
  private static Bag bagInPlace(final Version version, final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden, final Metadata metadata) throws NoSuchAlgorithmException, IOException{
    final Bag bag = new Bag(version);
    logger.info(messages.getString("creating_bag"), bag.getVersion(), root);
    bag.setRootDir(root);
    
    moveDataFilesIfNeeded(bag, includeHidden);
    
    createBagitFile(bag);

    createPayloadManifests(bag, algorithms, includeHidden);
    
    createMetadataFile(bag, metadata);
    
    createTagManifests(bag, algorithms, includeHidden);
    
    return bag;
  }
  
  private static void moveDataFilesIfNeeded(final Bag bag, final boolean includeHidden) throws IOException {
    if(bag.getVersion().isOlder(DOT_BAGIT_VERSION)) {
      Path tempDir = bag.getRootDir().resolve(System.currentTimeMillis() + ".temp");
      Files.createDirectory(tempDir);
      moveDataFiles(bag.getRootDir(), tempDir, includeHidden);
      Files.move(tempDir, PathUtils.getDataDir(bag));
    }
    else {
      final Path dotbagitDir = bag.getRootDir().resolve(".bagit");
      Files.createDirectories(dotbagitDir);
    }
  }
  
  private static void createBagitFile(final Bag bag) throws IOException{
    BagitFileWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), PathUtils.getBagitDir(bag));
  }
  
  private static void moveDataFiles(final Path rootDir, final Path dataDir, final boolean includeHidden) throws IOException{
    try(final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootDir)){
      for(final Path path : directoryStream){
        if(!path.equals(dataDir) && (!PathUtils.isHidden(path) || includeHidden)){
          Files.move(path, dataDir.resolve(path.getFileName()));
        }
      }
    }
  }
  
  private static Map<Manifest, MessageDigest> calculatePayloadManifests(final Bag bag, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    final Path dataDir = PathUtils.getDataDir(bag);
    logger.info(messages.getString("creating_payload_manifests"));
    final Map<Manifest, MessageDigest> payloadFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreatePayloadManifestsVistor payloadVisitor = new CreatePayloadManifestsVistor(payloadFilesMap, includeHidden);
    Files.walkFileTree(dataDir, payloadVisitor);
    
    return payloadFilesMap;
  }
  
  private static void createPayloadManifests(final Bag bag, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    final Map<Manifest, MessageDigest> payloadFilesMap = calculatePayloadManifests(bag, algorithms, includeHidden);
    bag.getPayLoadManifests().addAll(payloadFilesMap.keySet());
    ManifestWriter.writePayloadManifests(bag.getPayLoadManifests(), PathUtils.getBagitDir(bag), bag.getRootDir(), bag.getFileEncoding());
  }
  
  private static void createMetadataFile(final Bag bag, final Metadata metadata) throws IOException{
    bag.setMetadata(metadata);
    
    logger.debug(messages.getString("calculating_payload_oxum"), PathUtils.getDataDir(bag));
    final String payloadOxum = PathUtils.generatePayloadOxum(PathUtils.getDataDir(bag));
    bag.getMetadata().upsertPayloadOxum(payloadOxum);
    
    bag.getMetadata().remove("Bagging-Date"); //remove the old bagging date if it exists so that there is only one
    bag.getMetadata().add("Bagging-Date", new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).format(new Date()));
    
    logger.info(messages.getString("creating_metadata_file"));
    MetadataWriter.writeBagMetadata(bag.getMetadata(), bag.getVersion(), PathUtils.getBagitDir(bag), bag.getFileEncoding());
  }
  
  private static Map<Manifest, MessageDigest> calculateTagManifests(final Bag bag, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    logger.info(messages.getString("creating_tag_manifests"));
    final Map<Manifest, MessageDigest> tagFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreateTagManifestsVistor tagVistor = new CreateTagManifestsVistor(tagFilesMap, includeHidden);
    Files.walkFileTree(PathUtils.getBagitDir(bag), tagVistor);
    
    return tagFilesMap;
  }
  
  private static void createTagManifests(final Bag bag, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    final Map<Manifest, MessageDigest> tagFilesMap = calculateTagManifests(bag, algorithms, includeHidden);
    
    bag.getTagManifests().addAll(tagFilesMap.keySet());
    ManifestWriter.writeTagManifests(bag.getTagManifests(), PathUtils.getBagitDir(bag), bag.getRootDir(), bag.getFileEncoding());
  }
}
