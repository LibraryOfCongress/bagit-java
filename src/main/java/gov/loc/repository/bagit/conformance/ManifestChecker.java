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
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.util.PathUtils;

/**
 * Part of the BagIt conformance suite. This checker checks for various problems
 * related to the manifests in a bag.
 */
@SuppressWarnings({"PMD.UseLocaleWithCaseConversions"})
public final class ManifestChecker{

  private static final Logger logger = LoggerFactory.getLogger(ManifestChecker.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

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
          final Collection<BagitWarning> warningsToIgnore) throws IOException, InvalidBagitFileFormatException{

    boolean missingTagManifest = true;
    try(final DirectoryStream<Path> files = Files.newDirectoryStream(bagitDir)){
      for(final Path file : files){
        final String filename = PathUtils.getFilename(file);
        if(filename.contains("manifest-")){
          if(filename.startsWith("manifest-")){
            checkData(file, encoding, warnings, warningsToIgnore, true);
          } else{
            checkData(file, encoding, warnings, warningsToIgnore, false);
            missingTagManifest = false;
          }

          final String algorithm = filename.split("[-\\.]")[1];
          checkAlgorthm(algorithm, warnings, warningsToIgnore);
        }
      }
    }

    if(!warningsToIgnore.contains(BagitWarning.MISSING_TAG_MANIFEST) && missingTagManifest){
      logger.warn(messages.getString("bag_missing_tag_manifest_warning"), bagitDir);
      warnings.add(BagitWarning.MISSING_TAG_MANIFEST);
    }
  }

  /*
   * Check for a "bag within a bag" and for relative paths in the manifests
   */
  private static void checkData(final Path manifestFile, final Charset encoding, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest) throws IOException, InvalidBagitFileFormatException{
    try(final BufferedReader reader = Files.newBufferedReader(manifestFile, encoding)){
      final Set<String> paths = new HashSet<>();

      String line = reader.readLine();
      while(line != null){
        String path = parsePath(line);

        path = checkForManifestCreatedWithMD5SumTools(path, warnings, warningsToIgnore);

        if(!warningsToIgnore.contains(BagitWarning.DIFFERENT_CASE) && paths.contains(path.toLowerCase())){
          logger.warn(messages.getString("different_case_warning"), manifestFile, path);
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
  }

  static String parsePath(final String line) throws InvalidBagitFileFormatException{
    final String[] parts = line.split("\\s+", 2);
    if(parts.length < 2){
      final String formattedMessage = messages.getString("manifest_line_violated_spec_error");
      throw new InvalidBagitFileFormatException(MessageFormatter.format(formattedMessage, line).getMessage());
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
      logger.warn(messages.getString("md5sum_generated_line_warning"), path);
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
      if(dirToCheck == null){
        final String formattedMessage = messages.getString("cannot_access_parent_path_error");
        throw new IOException(MessageFormatter.format(formattedMessage, fileToCheck).getMessage()); //to satisfy findbugs
      }
      final String normalizedFileToCheck = normalizePathToNFD(fileToCheck);

      try(final DirectoryStream<Path> files = Files.newDirectoryStream(dirToCheck)){
        for(final Path file : files){
          final String normalizedFile = normalizePathToNFD(file);

          if(!file.equals(fileToCheck) && normalizedFileToCheck.equals(normalizedFile)){
            logger.warn(messages.getString("different_normalization_in_manifest_warning"), fileToCheck);
            warnings.add(BagitWarning.DIFFERENT_NORMALIZATION);
          }
        }
      }
    }
  }

  /*
   * Normalize to Canonical decomposition.
   */
  static String normalizePathToNFD(final Path path){
    return Normalizer.normalize(path.toString(), Normalizer.Form.NFD);
  }

  /*
   * check for a bag within a bag
   */
  private static void checkForBagWithinBag(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final boolean isPayloadManifest){
    if(!warningsToIgnore.contains(BagitWarning.BAG_WITHIN_A_BAG) && isPayloadManifest && line.contains("manifest-")){
      logger.warn(messages.getString("bag_within_bag_warning"));
      warnings.add(BagitWarning.BAG_WITHIN_A_BAG);
    }
  }

  /*
   * Check for relative paths (i.e. ./) in the manifest
   */
  private static void checkForRelativePaths(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final Path manifestFile){
    if(!warningsToIgnore.contains(BagitWarning.LEADING_DOT_SLASH) && line.contains("./")){
      logger.warn(messages.getString("leading_dot_slash_warning"), manifestFile, line);
      warnings.add(BagitWarning.LEADING_DOT_SLASH);
    }
  }

  /*
   * like .DS_Store or Thumbs.db
   */
  private static void checkForOSSpecificFiles(final String line, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore, final Path manifestFile){
    if(!warningsToIgnore.contains(BagitWarning.OS_SPECIFIC_FILES) && line.matches(OS_FILES_REGEX)){
      logger.warn(messages.getString("os_specific_files_warning"), manifestFile, line);
      warnings.add(BagitWarning.OS_SPECIFIC_FILES);
    }
  }

  /*
   * Check for anything weaker than SHA-512
   */
  static void checkAlgorthm(final String algorithm, final Set<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore){
    final String upperCaseAlg = algorithm.toUpperCase();
    if(!warningsToIgnore.contains(BagitWarning.WEAK_CHECKSUM_ALGORITHM)
            && (upperCaseAlg.startsWith("MD") || upperCaseAlg.matches("SHA(1|224|256|384)?"))){
      logger.warn(messages.getString("weak_algorithm_warning"), algorithm);
      warnings.add(BagitWarning.WEAK_CHECKSUM_ALGORITHM);
    } else if(!warningsToIgnore.contains(BagitWarning.NON_STANDARD_ALGORITHM) && !"SHA-512".equals(upperCaseAlg)){
      logger.warn(messages.getString("non_standard_algorithm_warning"), algorithm);
      warnings.add(BagitWarning.NON_STANDARD_ALGORITHM);
    }
  }

  //for unit test only
  static String getOsFilesRegex(){
    return OS_FILES_REGEX;
  }

}
