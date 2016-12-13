package gov.loc.repository.bagit.conformance;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.hash.BagitAlgorithmNameToSupportedAlgorithmMapping;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.util.PathUtils;
import gov.loc.repository.bagit.verify.BagVerifier;
import javafx.util.Pair;

/**
 * Responsible for checking a bag and providing insight into how it cause problems.
 */
@SuppressWarnings({"PMD.UseLocaleWithCaseConversions", "PMD.TooManyMethods", "PMD.GodClass"}) //TODO refactor
public class BagLinter {
  private static final Logger logger = LoggerFactory.getLogger(BagLinter.class);
  private static final Version LATEST_BAGIT_VERSION = new Version(0, 97);
  private static final String THUMBS_DB_FILE = "[Tt][Hh][Uu][Mm][Bb][Ss]\\.[Dd][Bb]";
  private static final String DS_STORE_FILE = "\\.[Dd][Ss]_[Ss][Tt][Oo][Rr][Ee]";
  private static final String OS_FILES_REGEX = ".*data/(" + THUMBS_DB_FILE + "|" + DS_STORE_FILE + ")";
  
  private final BagReader reader;
  
  public BagLinter(){
    reader = new BagReader();
  }
  
  public BagLinter(final BagitAlgorithmNameToSupportedAlgorithmMapping nameMapping){
    reader = new BagReader(nameMapping);
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
   * @throws MaliciousPathException If the manifest was crafted to try and access a file outside the bag directory
   */
  public Set<BagitWarning> lintBag(final Path rootDir, final Collection<BagitWarning> warningsToIgnore) throws IOException, UnparsableVersionException, InvalidBagMetadataException, MaliciousPathException, UnsupportedAlgorithmException{
    final Set<BagitWarning> warnings = new HashSet<>();
    
    //@Incubating
    Path bagitDir = rootDir.resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = rootDir;
    }
    
    final Path bagitFile = bagitDir.resolve("bagit.txt");
    final Pair<Version, Charset> bagitInfo = reader.readBagitTextFile(bagitFile);
    
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
   * Check for all the manifest specific potential problems
   */
  private void checkManifests(final Path bagitDir, final Charset encoding, final Set<BagitWarning> warnings, 
      final Collection<BagitWarning> warningsToIgnore) throws IOException, MaliciousPathException, UnsupportedAlgorithmException{
        
    final DirectoryStream<Path> files = Files.newDirectoryStream(bagitDir);
    for(final Path file : files){
      final String filename = PathUtils.getFilename(file);
      if(filename.contains("manifest-")){
        if(filename.startsWith("manifest-")){
          checkData(file, encoding, warnings, warningsToIgnore, true);
        }
        else{
          checkData(file, encoding, warnings, warningsToIgnore, false);
        }
        
        final String algorithm = filename.split("[-\\.]")[1];
        checkAlgorthm(algorithm, warnings, warningsToIgnore);
      }
    }
  }
  
  /*
   * Check for a "bag within a bag" and for relative paths in the manifests
   */
  private void checkData(final Path manifestFile, final Charset encoding, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest) throws IOException{
    final BufferedReader reader = Files.newBufferedReader(manifestFile, encoding);
    final Set<String> paths = new HashSet<>();
    
    String line = reader.readLine();
    while(line != null){
      String path = line.split("\\s+", 2)[1];
      
      path = checkForManifestCreatedWithMD5SumTools(path, warnings, warningsToIgnore);
      
      if(!warningsToIgnore.contains(BagitWarning.DIFFERENT_CASE) && paths.contains(path.toLowerCase())){
        logger.warn("In manifest [{}], path [{}] is the same as another path except for the case. This can cause problems if moving the bag to a filesystem that is case insensitive.", manifestFile, path);
        warnings.add(BagitWarning.DIFFERENT_CASE);
      }
      paths.add(path.toLowerCase());
      
      if(encoding.name().startsWith("UTF")){
        checkNormalization(path, manifestFile.getParent(), warnings, warningsToIgnore);
      }
      
      checkForBagWithinBag(line, warnings, warningsToIgnore, isPayloadManifest);
      
      checkForRelativePaths(line, warnings, warningsToIgnore, manifestFile);
      
      checkForOSSpecificFiles(line, warnings, warningsToIgnore, manifestFile);
      
      line = reader.readLine();
    }
  }
  
  /*
   * Check that the file specified has not changed its normalization (i.e. have the bytes changed but it still looks the same?)
   */
  private void checkNormalization(final String path, final Path rootDir, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore) throws IOException{
    if(!warningsToIgnore.contains(BagitWarning.DIFFERENT_NORMALIZATION)){
      
      final Path fileToCheck = rootDir.resolve(path).normalize();
      final Path dirToCheck = fileToCheck.getParent();
      if(dirToCheck == null){ throw new IOException("Could not access parent folder of " + fileToCheck);} //to satisfy findbugs
      final String normalizedFileToCheck = normalizePathToNFD(fileToCheck);
      
      final DirectoryStream<Path> files = Files.newDirectoryStream(dirToCheck);
      
      for(final Path file : files){
        final String normalizedFile = normalizePathToNFD(file);
        
        if(!file.equals(fileToCheck) && normalizedFileToCheck.equals(normalizedFile)){
          logger.warn("File [{}] has a different normalization then what is specified in the manifest.", fileToCheck);
          warnings.add(BagitWarning.DIFFERENT_NORMALIZATION);
        }
      }
    }
  }
  
  /*
   * Normalize to Canonical decomposition.
   */
  private String normalizePathToNFD(final Path path){
    final Path filename = path.getFileName();
    if(filename != null){
      return Normalizer.normalize(filename.toString(), Normalizer.Form.NFD);
    }
    
    return Normalizer.normalize(path.toString(), Normalizer.Form.NFD);
  }
  
  private String checkForManifestCreatedWithMD5SumTools(final String path, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    String fixedPath = path;
    final boolean startsWithStar = path.charAt(0) == '*';
    
    if(startsWithStar){
      fixedPath = path.substring(1);
    }
    
    if(!warningsToIgnore.contains(BagitWarning.MD5SUM_TOOL_GENERATED_MANIFEST) && startsWithStar){
      logger.warn("Path [{}] starts with a *, which means it was generated with a non-bagit tool. "
          + "It is recommended to remove the * in order to conform to the bagit specification.", path);
      warnings.add(BagitWarning.MD5SUM_TOOL_GENERATED_MANIFEST);
    }
    
    return fixedPath;
  }
  
  /*
   * check for a bag within a bag
   */
  private void checkForBagWithinBag(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest){
    if(!warningsToIgnore.contains(BagitWarning.BAG_WITHIN_A_BAG) && isPayloadManifest && line.contains("manifest-")){
      logger.warn("We stronger recommend not storing a bag within a bag as it is known to cause problems.");
      warnings.add(BagitWarning.BAG_WITHIN_A_BAG);
    }
  }
  
  /*
   * Check for relative paths (i.e. ./) in the manifest
   */
  private void checkForRelativePaths(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final Path manifestFile){
    if(!warningsToIgnore.contains(BagitWarning.LEADING_DOT_SLASH) && line.contains("./")){
      logger.warn("In manifest [{}] line [{}] is a non-normalized path.", manifestFile, line);
      warnings.add(BagitWarning.LEADING_DOT_SLASH);
    }
  }
  
  private void checkForOSSpecificFiles(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final Path manifestFile){
    if(!warningsToIgnore.contains(BagitWarning.OS_SPECIFIC_FILES) && line.matches(OS_FILES_REGEX)){
      logger.warn("In manifest [{}] line [{}] contains a OS specific file.", manifestFile, line);
      warnings.add(BagitWarning.OS_SPECIFIC_FILES);
    }
  }
  
  /*
   * Check for anything weaker than SHA-512
   */
  private void checkAlgorthm(final String algorithm, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    final String upperCaseAlg = algorithm.toUpperCase();
    if(!warningsToIgnore.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM) && 
        (upperCaseAlg.startsWith("MD") || upperCaseAlg.matches("SHA(-224|-256|-384)?"))){
      logger.warn("Detected a known weak algorithm [{}]. With the great advances in computer hardware there is little penalty "
          + "to using more bits to calculate the checksum.", algorithm);
      warnings.add(BagitWarning.WEAK_CHECKSUM_ALGORITHM);
    }
    
    else if(!warningsToIgnore.contains(BagitWarning.NON_STANDARD_ALGORITHM) && !"SHA-512".equals(upperCaseAlg)){
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
      final List<Pair<String, String>> metadata = reader.readBagMetadata(bagitDir, encoding);
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

  public BagReader getReader() {
    return reader;
  }
}
