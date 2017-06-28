package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.annotation.Incubating;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.writer.BagitFileWriter;
import gov.loc.repository.bagit.writer.ManifestWriter;

/**
 * Responsible for creating a bag in place.
 */
public final class BagCreator {
  private static final Logger logger = LoggerFactory.getLogger(BagCreator.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  private BagCreator(){}
  
  @SuppressWarnings("CPD-START")
  /**
   * Creates a basic(only required elements) bag in place for version 0.97.
   * This method moves and creates files, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithms an collection of {@link SupportedAlgorithm} implementations
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing or moving file(s)
   * @return a {@link Bag} object representing the newly created bagit bag
   */
  public static Bag bagInPlace(final Path root, final Collection<SupportedAlgorithm> algorithms, final boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    final Bag bag = new Bag(new Version(0, 97));
    bag.setRootDir(root);
    logger.info(messages.getString("creating_bag"), bag.getVersion(), root);
    
    final Path dataDir = root.resolve("data");
    Files.createDirectory(dataDir);
    try(final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)){
      for(final Path path : directoryStream){
        if(!path.equals(dataDir) && (!Files.isHidden(path) || includeHidden)){
          Files.move(path, dataDir.resolve(path.getFileName()));
        }
      }
    }
    
    logger.info(messages.getString("creating_payload_manifests"));
    final Map<Manifest, MessageDigest> payloadFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreatePayloadManifestsVistor payloadVisitor = new CreatePayloadManifestsVistor(payloadFilesMap, includeHidden);
    Files.walkFileTree(dataDir, payloadVisitor);
    
    bag.getPayLoadManifests().addAll(payloadFilesMap.keySet());
    BagitFileWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), root);
    ManifestWriter.writePayloadManifests(bag.getPayLoadManifests(), root, root, bag.getFileEncoding());
    
    logger.info(messages.getString("creating_tag_manifests"));
    final Map<Manifest, MessageDigest> tagFilesMap = Hasher.createManifestToMessageDigestMap(algorithms);
    final CreateTagManifestsVistor tagVistor = new CreateTagManifestsVistor(tagFilesMap, includeHidden);
    Files.walkFileTree(root, tagVistor);
    
    bag.getTagManifests().addAll(tagFilesMap.keySet());
    ManifestWriter.writeTagManifests(bag.getTagManifests(), root, root, bag.getFileEncoding());
    
    return bag;
  }
  
  @SuppressWarnings("CPD-END")
  /**
   * Creates a basic(only required elements) .bagit bag in place.
   * This creates files and directories, thus if an error is thrown during operation it may leave the filesystem 
   * in an unknown state of transition. Thus this is <b>not thread safe</b>
   * 
   * @param root the directory that will become the base of the bag and where to start searching for content
   * @param algorithms an collection of {@link SupportedAlgorithm} implementations
   * @param includeHidden to include hidden files when generating the bagit files, like the manifests
   * @return a {@link Bag} object representing the newly created bagit bag
   * @throws NoSuchAlgorithmException if {@link MessageDigest} can't find the algorithm
   * @throws IOException if there is a problem writing files or .bagit directory
   */
  @Incubating
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