package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.verify.Verifier;
import gov.loc.repository.bagit.writer.BagWriter;

public class BagCreator {
  private static final Logger logger = LoggerFactory.getLogger(Verifier.class);
  
  /**
   * Creates a basic(only required elements) bag in place.
   * @param algorithm - digest algorithm name, not bagit algorithm name
   */
  public static Bag bagInPlace(File root, StandardSupportedAlgorithms algorithm, boolean includeHidden) throws NoSuchAlgorithmException, IOException{
    Bag bag = new Bag();
    bag.setRootDir(root);
    
    File[] files = root.listFiles();
    File dataDir = new File(root, "data");
    if(!dataDir.exists() && !dataDir.mkdir()){
      throw new IOException("Unable to make " + dataDir);
    }
    
    moveFilesToDataDir(files, dataDir);
    
    Manifest manifest = new Manifest(algorithm.name().toLowerCase());
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getMessageDigestName());
    AddPayloadToBagManifestVistor visitor = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHidden);
    Files.walkFileTree(Paths.get(dataDir.toURI()), visitor);
    
    bag.getPayLoadManifests().add(manifest);
    BagWriter.writeBagitFile(bag.getVersion(), bag.getFileEncoding(), root);
    BagWriter.writePayloadManifests(bag.getPayLoadManifests(), root, bag.getFileEncoding());
    
    
    return bag;
  }
  
  protected static void moveFilesToDataDir(File[] files, File dataDir) throws IOException{
    if(files != null){
      for(File file : files){
        Path dest = Paths.get(dataDir.getPath(), file.getName());
        logger.debug("Moving [{}] to [{}]", file, dest);
        Files.move(Paths.get(file.toURI()), dest);
      }
    }
  }
}