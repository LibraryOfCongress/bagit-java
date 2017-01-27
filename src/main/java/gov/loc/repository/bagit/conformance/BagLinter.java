package gov.loc.repository.bagit.conformance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagMetadataException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.reader.BagitTextFileReader;
import gov.loc.repository.bagit.verify.BagVerifier;

/**
 * Responsible for checking a bag and providing insight into how it cause problems.
 */
public class BagLinter {
  private static final Logger logger = LoggerFactory.getLogger(BagLinter.class);
  
  
  private final BagReader reader;
  
  public BagLinter(){
    reader = new BagReader();
  }
  
  /**
   * Check a bag against a bagit-profile as described by {@link https://github.com/ruebot/bagit-profiles}
   * 
   * @param jsonProfile the json string describing the profile
   * @param bag the bag to check against the profile
   * 
   * @return true if the bag conforms to the bagit profile, false otherwise
   */
  public boolean checkAgainstProfile(final String jsonProfile, final Bag bag){
    return BagProfileChecker.bagConformsToProfile(jsonProfile, bag);
  }
  
  /**
   * The BagIt specification is very flexible in what it allows which leads to situations 
   * where something may be technically allowed, but should be discouraged.
   * This method checks a bag for potential problems, or other items that are allowed but discouraged.
   * This <strong>does not</strong> validate a bag. See {@link BagVerifier} instead.
   * 
   * @param rootDir the root directory of the bag
   * 
   * @return a set of {@link BagitWarning} detailing all items that should be fixed.
   * 
   * @throws InvalidBagMetadataException if the bag metadata does not conform to the bagit specification
   * @throws UnparsableVersionException if there is an error reading the bagit version
   * @throws IOException if there was an error reading a file
   * @throws UnsupportedAlgorithmException if the {@link BagReader} does not support reading the manifest 
   * @throws MaliciousPathException If the manifest was crafted to try and access a file outside the bag directory
   * @throws InvalidBagitFileFormatException if one or more of the files do not correctly follow the bagit specification format
   */
  public Set<BagitWarning> lintBag(final Path rootDir) throws IOException, UnparsableVersionException, InvalidBagMetadataException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    return this.lintBag(rootDir, Collections.emptyList());
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
   * @throws InvalidBagitFileFormatException if one or more of the files do not correctly follow the bagit specification format
   */
  public Set<BagitWarning> lintBag(final Path rootDir, final Collection<BagitWarning> warningsToIgnore) throws IOException, UnparsableVersionException, InvalidBagMetadataException, MaliciousPathException, UnsupportedAlgorithmException, InvalidBagitFileFormatException{
    final Set<BagitWarning> warnings = new HashSet<>();
    
    //@Incubating
    Path bagitDir = rootDir.resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = rootDir;
    }
    
    logger.debug("Reading bagit.txt file for version and encoding.");
    final Path bagitFile = bagitDir.resolve("bagit.txt");
    final SimpleImmutableEntry<Version, Charset> bagitInfo = BagitTextFileReader.readBagitTextFile(bagitFile);
    
    logger.debug("Checking encoding problems.");
    EncodingChecker.checkEncoding(bagitInfo.getValue(), warnings, warningsToIgnore);
    
    logger.debug("checking for latest version.");
    VersionChecker.checkVersion(bagitInfo.getKey(), warnings, warningsToIgnore);
    
    logger.debug("checking manifests for problems.");
    ManifestChecker.checkManifests(bagitDir, bagitInfo.getValue(), warnings, warningsToIgnore);

    logger.debug("checking bag metadata for problems.");
    MetadataChecker.checkBagMetadata(bagitDir, bagitInfo.getValue(), warnings, warningsToIgnore);
    
    return warnings;
  }
  
  public BagReader getReader() {
    return reader;
  }
}
