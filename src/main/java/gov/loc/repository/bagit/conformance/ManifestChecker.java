package gov.loc.repository.bagit.conformance;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * Part of the BagIt conformance suite. 
 * This checker checks for various problems related to the manifests in a bag.
 */
@SuppressWarnings({"PMD.UseLocaleWithCaseConversions"})
public final class ManifestChecker {
  private static final Logger logger = LoggerFactory.getLogger(ManifestChecker.class);
  
  private static final String THUMBS_DB_FILE = "[Tt][Hh][Uu][Mm][Bb][Ss]\\.[Dd][Bb]";
  private static final String DS_STORE_FILE = "\\.[Dd][Ss]_[Ss][Tt][Oo][Rr][Ee]";
  private static final String SPOTLIGHT_FILE = "\\.[Ss][Pp][Oo][Tt][Ll][Ii][Gg][Hh][Tt]-[Vv]100";
  private static final String TRASHES_FILE = "\\.(_.)?[Tt][Rr][Aa][Ss][Hh][Ee][Ss]";
  private static final String FS_EVENTS_FILE = "\\.[Ff][Ss][Ee][Vv][Ee][Nn][Tt][Ss][Dd]";
  private static final String OS_FILES_REGEX = ".*data/(" + THUMBS_DB_FILE + "|" + DS_STORE_FILE + "|" + SPOTLIGHT_FILE + "|" + TRASHES_FILE + "|" + FS_EVENTS_FILE + ")";
  
  private ManifestChecker(){
    //intentionally left empty
  }
  
  /*
   * Check for all the manifest specific potential problems
   */
  public static void checkManifests(final Path bagitDir, final Charset encoding, final Set<BagitWarning> warnings, 
      final Collection<BagitWarning> warningsToIgnore) throws IOException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
        
    boolean missingTagManifest = true;
    final DirectoryStream<Path> files = Files.newDirectoryStream(bagitDir);
    for(final Path file : files){
      final String filename = PathUtils.getFilename(file);
      if(filename.contains("manifest-")){
        if(filename.startsWith("manifest-")){
          checkData(file, encoding, warnings, warningsToIgnore, true);
        }
        else{
          checkData(file, encoding, warnings, warningsToIgnore, false);
          missingTagManifest = false;
        }
        
        final String algorithm = filename.split("[-\\.]")[1];
        checkAlgorthm(algorithm, warnings, warningsToIgnore);
      }
    }
    
    if(!warningsToIgnore.contains(BagitWarning.MISSING_TAG_MANIEST) && missingTagManifest){
      logger.warn("Bag [{}] does not contain a tag manifest, which is always recommended.", bagitDir);
      warnings.add(BagitWarning.MISSING_TAG_MANIEST);
    }
  }
  
  /*
   * Check for a "bag within a bag" and for relative paths in the manifests
   */
  private static void checkData(final Path manifestFile, final Charset encoding, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest) throws IOException, InvalidBagitFileFormatException{
    final BufferedReader reader = Files.newBufferedReader(manifestFile, encoding);
    final Set<String> paths = new HashSet<>();
    
    String line = reader.readLine();
    while(line != null){
      String path = parsePath(line);
      
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
  
  private static String parsePath(final String line) throws InvalidBagitFileFormatException{
    final String[] parts = line.split("\\s+", 2);
    if(parts.length < 2){
      throw new InvalidBagitFileFormatException("Manifest contains line [" + line + "] which does not follow the specified form of <CHECKSUM> <PATH>");
    }
    
    return parts[1];
  }
  
  private static String checkForManifestCreatedWithMD5SumTools(final String path, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
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
   * Check that the file specified has not changed its normalization (i.e. have the bytes changed but it still looks the same?)
   */
  private static void checkNormalization(final String path, final Path rootDir, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore) throws IOException{
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
  static String normalizePathToNFD(final Path path){
    final Path filename = path.getFileName();
    if(filename != null){
      return Normalizer.normalize(filename.toString(), Normalizer.Form.NFD);
    }
    
    return Normalizer.normalize(path.toString(), Normalizer.Form.NFD);
  }
  
  /*
   * check for a bag within a bag
   */
  private static void checkForBagWithinBag(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest){
    if(!warningsToIgnore.contains(BagitWarning.BAG_WITHIN_A_BAG) && isPayloadManifest && line.contains("manifest-")){
      logger.warn("We stronger recommend not storing a bag within a bag as it is known to cause problems.");
      warnings.add(BagitWarning.BAG_WITHIN_A_BAG);
    }
  }
  
  /*
   * Check for relative paths (i.e. ./) in the manifest
   */
  private static void checkForRelativePaths(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final Path manifestFile){
    if(!warningsToIgnore.contains(BagitWarning.LEADING_DOT_SLASH) && line.contains("./")){
      logger.warn("In manifest [{}] line [{}] is a non-normalized path.", manifestFile, line);
      warnings.add(BagitWarning.LEADING_DOT_SLASH);
    }
  }
  
  /*
   * like .DS_Store or Thumbs.db
   */
  private static void checkForOSSpecificFiles(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final Path manifestFile){
    if(!warningsToIgnore.contains(BagitWarning.OS_SPECIFIC_FILES) && line.matches(OS_FILES_REGEX)){
      logger.warn("In manifest [{}] line [{}] contains a OS specific file.", manifestFile, line);
      warnings.add(BagitWarning.OS_SPECIFIC_FILES);
    }
  }
  
  /*
   * Check for anything weaker than SHA-512
   */
  private static void checkAlgorthm(final String algorithm, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
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

  //for unit test only
  static String getOsFilesRegex() {
    return OS_FILES_REGEX;
  }

}
