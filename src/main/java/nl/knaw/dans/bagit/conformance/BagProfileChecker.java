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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import nl.knaw.dans.bagit.conformance.profile.BagInfoRequirement;
import nl.knaw.dans.bagit.conformance.profile.BagitProfile;
import nl.knaw.dans.bagit.conformance.profile.BagitProfileDeserializer;
import nl.knaw.dans.bagit.domain.Bag;
import nl.knaw.dans.bagit.domain.FetchItem;
import nl.knaw.dans.bagit.domain.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import nl.knaw.dans.bagit.domain.Metadata;
import nl.knaw.dans.bagit.exceptions.conformance.BagitVersionIsNotAcceptableException;
import nl.knaw.dans.bagit.exceptions.conformance.FetchFileNotAllowedException;
import nl.knaw.dans.bagit.exceptions.conformance.MetatdataValueIsNotAcceptableException;
import nl.knaw.dans.bagit.exceptions.conformance.MetatdataValueIsNotRepeatableException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredManifestNotPresentException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredMetadataFieldNotPresentException;
import nl.knaw.dans.bagit.exceptions.conformance.RequiredTagFileNotPresentException;

/**
 * Responsible for checking a bag against a profile
 */
public final class BagProfileChecker {
  private static final Logger logger = LoggerFactory.getLogger(BagProfileChecker.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

  private BagProfileChecker(){
    //intentionally left empty
  }
  
  /**
   * Check a bag against a bagit-profile as described by 
   * <a href="https://github.com/ruebot/bagit-profiles">https://github.com/ruebot/bagit-profiles</a>
   * <br>Note: <b> This implementation does not check the Serialization part of the profile!</b>
   * 
   * @param jsonProfile the input stream to the json string describing the profile
   * @param bag the bag to check against the profile
   * 
   * @throws IOException if there is a problem reading the profile
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
  public static void bagConformsToProfile(final InputStream jsonProfile, final Bag bag) throws JsonParseException, JsonMappingException,
  IOException, FetchFileNotAllowedException, RequiredMetadataFieldNotPresentException, MetatdataValueIsNotAcceptableException, 
  RequiredManifestNotPresentException, BagitVersionIsNotAcceptableException, RequiredTagFileNotPresentException, MetatdataValueIsNotRepeatableException{
    
    final BagitProfile profile = parseBagitProfile(jsonProfile);
    checkFetch(bag.getRootDir(), profile.isFetchFileAllowed(), bag.getItemsToFetch());
    
    checkMetadata(bag.getMetadata(), profile.getBagInfoRequirements());
    
    requiredManifestsExist(bag.getPayLoadManifests(), profile.getManifestTypesRequired(), true);

    requiredManifestsExist(bag.getTagManifests(), profile.getTagManifestTypesRequired(), false);

    if(!profile.getAcceptableBagitVersions().contains(bag.getVersion().toString())){
      throw new BagitVersionIsNotAcceptableException(messages.getString("bagit_version_not_acceptable_error"), bag.getVersion(), profile.getAcceptableBagitVersions());
    }
    
    requiredTagFilesExist(bag.getRootDir(), profile.getTagFilesRequired());
  }
  
  private static BagitProfile parseBagitProfile(final InputStream jsonProfile) throws JsonParseException, JsonMappingException, IOException{
    final ObjectMapper mapper = new ObjectMapper();
    final SimpleModule module = new SimpleModule();
    module.addDeserializer(BagitProfile.class, new BagitProfileDeserializer());
    mapper.registerModule(module);

    return mapper.readValue(jsonProfile, BagitProfile.class);
  }
  
  private static void checkFetch(final Path rootDir, final boolean allowFetchFile, final List<FetchItem> itemsToFetch) throws FetchFileNotAllowedException{
    logger.debug(messages.getString("checking_fetch_file_allowed"), rootDir);
    if(!allowFetchFile && !itemsToFetch.isEmpty()){
      throw new FetchFileNotAllowedException(messages.getString("fetch_file_not_allowed_error"), rootDir);
    }
  }
  
  private static void checkMetadata(final Metadata bagMetadata, final Map<String, BagInfoRequirement> bagInfoEntryRequirements)
      throws RequiredMetadataFieldNotPresentException, MetatdataValueIsNotAcceptableException, MetatdataValueIsNotRepeatableException{
    
    for(final Entry<String, BagInfoRequirement> bagInfoEntryRequirement : bagInfoEntryRequirements.entrySet()){
      final boolean metadataContainsKey = bagMetadata.contains(bagInfoEntryRequirement.getKey());
      
      checkIfMetadataEntryIsRequired(bagInfoEntryRequirement, metadataContainsKey);
      
      checkForAcceptableValues(bagMetadata, bagInfoEntryRequirement);
      
      checkForNoneRepeatableMetadata(bagMetadata, bagInfoEntryRequirement, metadataContainsKey);
    }
  }
  
  private static void checkIfMetadataEntryIsRequired(final Entry<String, BagInfoRequirement> bagInfoEntryRequirement, final boolean metadataContainsKey) throws RequiredMetadataFieldNotPresentException{
    logger.debug(messages.getString("checking_metadata_entry_required"), bagInfoEntryRequirement.getKey());
    //is it required and not there?
    if(bagInfoEntryRequirement.getValue().isRequired() && !metadataContainsKey){
      throw new RequiredMetadataFieldNotPresentException(messages.getString("required_metadata_field_not_present_error"), bagInfoEntryRequirement.getKey());
    }
  }
  
  private static void checkForAcceptableValues(final Metadata bagMetadata, final Entry<String, BagInfoRequirement> bagInfoEntryRequirement) throws MetatdataValueIsNotAcceptableException{
    //a size of zero implies that all values are acceptable
    if(!bagInfoEntryRequirement.getValue().getAcceptableValues().isEmpty()){
      logger.debug(messages.getString("check_values_acceptable"), bagInfoEntryRequirement.getKey());
      for(final String metadataValue : bagMetadata.get(bagInfoEntryRequirement.getKey())){
        if(!bagInfoEntryRequirement.getValue().getAcceptableValues().contains(metadataValue)){
          throw new MetatdataValueIsNotAcceptableException(messages.getString("metadata_value_not_acceptable_error"), 
              bagInfoEntryRequirement.getKey(), bagInfoEntryRequirement.getValue().getAcceptableValues(), metadataValue);
        }
      }
    }
  }
  
  private static void checkForNoneRepeatableMetadata(final Metadata bagMetadata, final Entry<String, BagInfoRequirement> bagInfoEntryRequirement, final boolean metadataContainsKey) throws MetatdataValueIsNotRepeatableException{
    //if it is none repeatable, but shows up multiple times
    if(!bagInfoEntryRequirement.getValue().isRepeatable() && metadataContainsKey 
        && bagMetadata.get(bagInfoEntryRequirement.getKey()).size() > 1){
      throw new MetatdataValueIsNotRepeatableException(messages.getString("metadata_value_not_repeatable_error"), bagInfoEntryRequirement.getKey());
    }
  }
  
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static void requiredManifestsExist(final Set<Manifest> manifests, final List<String> requiredManifestTypes, final boolean isPayloadManifest) throws RequiredManifestNotPresentException{
    final Set<String> manifestTypesPresent = new HashSet<>();
    logger.debug(messages.getString("check_required_manifests_present"));
    
    for(final Manifest manifest : manifests){
      manifestTypesPresent.add(manifest.getAlgorithm().getBagitName());
    }
    
    for(final String requiredManifestType : requiredManifestTypes){
      if(!manifestTypesPresent.contains(requiredManifestType)){
        final StringBuilder sb = new StringBuilder();
        if(isPayloadManifest){ sb.append("tag");
          sb.append(MessageFormatter.format(messages.getString("required_tag_manifest_type_not_present"), requiredManifestType).getMessage());
        }
        else{
          sb.append(MessageFormatter.format(messages.getString("required_manifest_type_not_present"), requiredManifestType).getMessage());
        }
          
        throw new RequiredManifestNotPresentException(sb.toString());
      }
    }
  }
  
  private static void requiredTagFilesExist(final Path rootDir, final List<String> requiredTagFilePaths) throws RequiredTagFileNotPresentException{
    Path requiredTagFile;
    logger.debug(messages.getString("checking_required_tag_file_exists"));
    
    for(final String requiredTagFilePath : requiredTagFilePaths){
      requiredTagFile = rootDir.resolve(requiredTagFilePath);
      if(!Files.exists(requiredTagFile)){
        throw new RequiredTagFileNotPresentException(messages.getString("required_tag_file_not_found_error"), requiredTagFilePath);
      }
    }
  }
}
