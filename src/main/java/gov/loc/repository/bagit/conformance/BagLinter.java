package gov.loc.repository.bagit.conformance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.MaliciousManifestException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.util.PathUtils;
import gov.loc.repository.bagit.verify.BagVerifier;
import gov.loc.repository.bagit.warning.BagitWarning;
import javafx.util.Pair;

/**
 * Responsible for checking a bag and providing insight into how it cause problems.
 */
public class BagLinter extends BagReader {
  private static final Logger logger = LoggerFactory.getLogger(BagLinter.class);
  private static final Version LATEST_BAGIT_VERSION = new Version(0, 97);
  
  public BagLinter(){
    super();
  }
  
  public BagLinter(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    super(nameMapping);
  }
  
  /**
   * The BagIt specification is very flexible in what it allows which leads to situations 
   * where something may be technically allowed, but should be discouraged.
   * This method checks a bag for potential problems, or other items that are allowed but discouraged.
   * This <strong>does not</strong> validate a bag. See {@link BagVerifier} instead.
   * 
   * @param rootDir the root directory of the bag
   * @param warningsToIgnore any {@link BagitWarning} to ignore when linting
   * 
   * @return a set of {@link BagitWarning} detailing all items that should be fixed.
   * 
   * @throws InvalidBagMetadataException if the bag metadata does not conform to the bagit specification
   * @throws UnparsableVersionException if there is an error reading the bagit version
   * @throws IOException if there was an error reading a file
   * @throws UnsupportedAlgorithmException if the {@link BagReader} does not support reading the manifest 
   * @throws MaliciousManifestException If the manifest was crafted to try and access a file outside the bag directory
   */
  public Set<BagitWarning> lintBag(final Path rootDir, final Collection<BagitWarning> warningsToIgnore) throws IOException, UnparsableVersionException, InvalidBagMetadataException, MaliciousManifestException, UnsupportedAlgorithmException{
    final Set<BagitWarning> warnings = new HashSet<>();
    
    //@Incubating
    Path bagitDir = rootDir.resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = rootDir;
    }
    
    final Path bagitFile = bagitDir.resolve("bagit.txt");
    final Pair<Version, Charset> bagitInfo = readBagitTextFile(bagitFile);
    
    checkEncoding(bagitInfo.getValue(), warnings, warningsToIgnore);
    
    checkVersion(bagitInfo.getKey(), warnings, warningsToIgnore);
    
    checkManifests(bagitDir, bagitInfo.getValue(), warnings, warningsToIgnore);

    checkForPayloadOxumMetadata(bagitDir, bagitInfo.getValue(), warnings, warningsToIgnore);
    
    return warnings;
  }
  
  /*
   * It is now normal for all files to be UTF-8
   */
  private void checkEncoding(final Charset encoding, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    if(!warningsToIgnore.contains(BagitWarning.TAG_FILES_ENCODING) && !StandardCharsets.UTF_8.equals(encoding)){
      logger.warn("Tag files are encoded with [{}]. We recommend always using UTF-8 instead.", encoding);
      warnings.add(BagitWarning.TAG_FILES_ENCODING);
    }
  }
  
  /*
   * Check that they are using the latest version
   */
  private void checkVersion(final Version version, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    if(!warningsToIgnore.contains(BagitWarning.OLD_BAGIT_VERSION) && LATEST_BAGIT_VERSION.compareTo(version) == 1){
      logger.warn("Found version [{}] of the bagit specification but the latest version is [{}].", version, LATEST_BAGIT_VERSION);
      warnings.add(BagitWarning.OLD_BAGIT_VERSION);
    }
  }
  
  /*
   * Check for all the manifest specific potentional problems
   */
  private void checkManifests(final Path bagitDir, final Charset encoding, final Set<BagitWarning> warnings, 
      final Collection<BagitWarning> warningsToIgnore) throws IOException, MaliciousManifestException, UnsupportedAlgorithmException{
    
    final DirectoryStream<Path> manifestPaths = getAllManifestFiles(bagitDir);
    for(final Path manifestPath : manifestPaths){
      final Manifest manifest = readManifest(manifestPath, bagitDir, encoding);
      final String filename = PathUtils.getFilename(manifestPath);
      
      if(filename.startsWith("manifest-")){
        checkData(manifest.getFileToChecksumMap().keySet(), warnings, warningsToIgnore, true);
      }
      else{
        checkData(manifest.getFileToChecksumMap().keySet(), warnings, warningsToIgnore, false);
      }
      checkAlgorthm(manifest.getAlgorithm().getMessageDigestName(), warnings, warningsToIgnore);
    }
  }
  
  /*
   * Check for a "bag within a bag" and for relative paths in the manifests
   */
  private void checkData(final Set<Path> data, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest){
    for(final Path dataPath : data){
      final String path = dataPath.toString();
      
      if(!warningsToIgnore.contains(BagitWarning.BAG_WITHIN_A_BAG) && isPayloadManifest && path.contains("manifest-")){
        logger.warn("We stronger recommend not storing a bag within a bag as it is known to cause problems.");
        warnings.add(BagitWarning.BAG_WITHIN_A_BAG);
      }
      
      if(!warningsToIgnore.contains(BagitWarning.LEADING_DOT_SLASH) && path.contains("./")){
        logger.warn("Found path [{}] which contains a non-normalized path(i.e. a leading ./ for the relative path)", path);
        warnings.add(BagitWarning.LEADING_DOT_SLASH);
      }
    }
  }
  
  /*
   * Check for anything weaker than SHA-512
   */
  private void checkAlgorthm(final String algorithm, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    
    if(!warningsToIgnore.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM) && 
        (algorithm.startsWith("MD") || algorithm.matches("SHA(-224|-256|-384)?"))){
      logger.warn("Detected a known weak algorithm [{}]. With the great advances in computer hardware there is little penalty "
          + "to using more bits to calculate the checksum.", algorithm);
      warnings.add(BagitWarning.WEAK_CHECKSUM_ALGORITHM);
    }
    
    else if(!warningsToIgnore.contains(BagitWarning.NON_STANDARD_ALGORITHM) && !"SHA-512".equals(algorithm)){
      logger.warn("Detected algorithm [{}] which is not included by default in Java. This will make it more difficult "
          + "to read this bag on some systems. Consider changing it to SHA-512.", algorithm);
      warnings.add(BagitWarning.NON_STANDARD_ALGORITHM);
    }
  }
  
  /*
   * Check that the metadata contains the Payload-Oxum key-value pair
   */
  private void checkForPayloadOxumMetadata(final Path bagitDir, final Charset encoding, final Set<BagitWarning> warnings, 
      final Collection<BagitWarning> warningsToIgnore) throws IOException, InvalidBagMetadataException{
    if(!warningsToIgnore.contains(BagitWarning.PAYLOAD_OXUM_MISSING)){
      final List<Pair<String, String>> metadata = readBagMetadata(bagitDir, encoding);
      boolean containsPayloadOxum = false;
      
      for(final Pair<String, String> pair : metadata){
        if("Payload-Oxum".equals(pair.getKey())){
          containsPayloadOxum = true;
        }
      }
      
      if(!containsPayloadOxum){
        warnings.add(BagitWarning.PAYLOAD_OXUM_MISSING);
      }
    }
  }
}
