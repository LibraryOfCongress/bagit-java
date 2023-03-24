/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.conformance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import nl.knaw.dans.bagit.conformance.profile.BagitProfile;
import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;
import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.MaliciousPathException;
import nl.knaw.dans.bagit.exceptions.UnparsableVersionException;
import nl.knaw.dans.bagit.exceptions.UnsupportedAlgorithmException;
import nl.knaw.dans.bagit.reader.BagitTextFileReader;
import nl.knaw.dans.bagit.reader.KeyValueReader;
import nl.knaw.dans.bagit.verify.BagVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import nl.knaw.dans.bagit.domain.Version;
import nl.knaw.dans.bagit.exceptions.conformance.BagitVersionIsNotAcceptableException;
import nl.knaw.dans.bagit.exceptions.conformance.FetchFileNotAllowedException;
import nl.knaw.dans.bagit.exceptions.conformance.MetatdataValueIsNotAcceptableException;
import nl.knaw.dans.bagit.exceptions.conformance.MetatdataValueIsNotRepeatableException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredManifestNotPresentException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredMetadataFieldNotPresentException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredTagFileNotPresentException;

/**
 * Responsible for checking a bag and providing insight into how it cause problems.
 * This class is only to be used on VALID bags, using it on un-validated bags may result in
 * exceptions being thrown (like {@link java.io.IOException} )
 */
public final class BagLinter {
  private static final Logger logger = LoggerFactory.getLogger(BagLinter.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private static final Version VERSION_1_0 = new Version(1,0);
  
  private BagLinter(){
//    intentionally left empty
  }
  
  /**
   * Check a bag against a bagit-profile as described by 
   * <a href="https://github.com/ruebot/bagit-profiles">https://github.com/ruebot/bagit-profiles</a>
   * <br>Note: <b> This implementation does not check the Serialization part of the profile!</b>
   * 
   * @param jsonProfile the input stream to the json string describing the profile
   * @param bag the bag to check against the profile
   * 
   * @throws IOException if there is a problem reading the profile or some of the bag files
   * @throws JsonMappingException if there is a problem mapping the profile to the {@link BagitProfile}
   * @throws JsonParseException if there is a problem parsing the json while mapping to java object
   * 
   * @throws FetchFileNotAllowedException if there is a fetch file when the profile prohibits it
   * @throws MetatdataValueIsNotAcceptableException if a metadata value is not in the list of acceptable values
   * @throws MetatdataValueIsNotRepeatableException if a metadata value shows up more than once when not repeatable
   * @throws RequiredMetadataFieldNotPresentException if a metadata field is not present but it should be
   * @throws RequiredManifestNotPresentException if a payload or tag manifest type is not present but should be
   * @throws BagitVersionIsNotAcceptableException if the version of the bag is not in the list of acceptable versions
   * @throws RequiredTagFileNotPresentException if a tag file is not present but should be
   */
  public static void checkAgainstProfile(final InputStream jsonProfile, final Bag bag) throws JsonParseException, JsonMappingException,
  IOException, FetchFileNotAllowedException, RequiredMetadataFieldNotPresentException, MetatdataValueIsNotAcceptableException, RequiredManifestNotPresentException, 
  BagitVersionIsNotAcceptableException, RequiredTagFileNotPresentException, MetatdataValueIsNotRepeatableException{
    BagProfileChecker.bagConformsToProfile(jsonProfile, bag);
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
   * @throws UnsupportedAlgorithmException if there is an error while reading one of the manifests due to the algorithm being unsupported
   * @throws MaliciousPathException if the path is crafted to be malicious (overwrite non bag files)
   */
  public static Set<BagitWarning> lintBag(final Path rootDir) throws IOException, UnparsableVersionException, InvalidBagMetadataException, InvalidBagitFileFormatException, MaliciousPathException, UnsupportedAlgorithmException{
    return lintBag(rootDir, Collections.emptyList());
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
   * @throws UnsupportedAlgorithmException if there is an error while reading one of the manifests due to the algorithm being unsupported
   * @throws MaliciousPathException if the path is crafted to be malicious (overwrite non bag files)
   */
  public static Set<BagitWarning> lintBag(final Path rootDir, final Collection<BagitWarning> warningsToIgnore) throws IOException, UnparsableVersionException, InvalidBagMetadataException, InvalidBagitFileFormatException, MaliciousPathException, UnsupportedAlgorithmException{
    final Set<BagitWarning> warnings = new HashSet<>();
    
    //@Incubating
    Path bagitDir = rootDir.resolve(".bagit");
    if(!Files.exists(bagitDir)){
      bagitDir = rootDir;
    }
    
    final Path bagitFile = bagitDir.resolve("bagit.txt");
    checkForExtraLines(bagitFile, warnings, warningsToIgnore);
    final SimpleImmutableEntry<Version, Charset> bagitInfo = BagitTextFileReader.readBagitTextFile(bagitFile);
    
    logger.info(messages.getString("checking_encoding_problems"));
    EncodingChecker.checkEncoding(bagitInfo.getValue(), warnings, warningsToIgnore);
    
    logger.info(messages.getString("checking_latest_version"));
    VersionChecker.checkVersion(bagitInfo.getKey(), warnings, warningsToIgnore);
    
    logger.info(messages.getString("checking_manifest_problems"));
    ManifestChecker.checkManifests(bagitInfo.getKey(), bagitDir, bagitInfo.getValue(), warnings, warningsToIgnore);

    logger.info(messages.getString("checking_metadata_problems"));
    MetadataChecker.checkBagMetadata(bagitDir, bagitInfo.getValue(), warnings, warningsToIgnore);
    
    return warnings;
  }
  
  private static void checkForExtraLines(final Path bagitFile, final Collection<BagitWarning> warnings, final Collection<BagitWarning> warningsToIgnore) throws InvalidBagMetadataException, IOException, UnparsableVersionException{
    if(warningsToIgnore.contains(BagitWarning.EXTRA_LINES_IN_BAGIT_FILES)){
      logger.debug(messages.getString("skipping_check_extra_lines"));
      return;
    }
    
    logger.debug(messages.getString("checking_extra_lines"));
    final List<SimpleImmutableEntry<String, String>> pairs = KeyValueReader.readKeyValuesFromFile(bagitFile, ":", StandardCharsets.UTF_8);
     
    for(final SimpleImmutableEntry<String, String> pair : pairs){
      if("BagIt-Version".equals(pair.getKey())){
        final Version version = BagitTextFileReader.parseVersion(pair.getValue());
        //versions before 1.0 specified it must be exactly 2 lines
        if(pairs.size() > 2 && version.isOlder(VERSION_1_0)){
          logger.warn(messages.getString("extra_lines_warning"), pairs.size());
          warnings.add(BagitWarning.EXTRA_LINES_IN_BAGIT_FILES);
        }
      }
    }
  }
}
