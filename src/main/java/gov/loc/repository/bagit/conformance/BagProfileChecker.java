package gov.loc.repository.bagit.conformance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import gov.loc.repository.bagit.conformance.profile.BagInfoEntry;
import gov.loc.repository.bagit.conformance.profile.BagitProfile;
import gov.loc.repository.bagit.conformance.profile.BagitProfileDeserializer;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.exceptions.conformance.BagitVersionIsNotAcceptable;
import gov.loc.repository.bagit.exceptions.conformance.FetchFileNotAllowedException;
import gov.loc.repository.bagit.exceptions.conformance.MetatdataValueIsNotAcceptable;
import gov.loc.repository.bagit.exceptions.conformance.RequiredManifestNotPresent;
import gov.loc.repository.bagit.exceptions.conformance.RequiredMetadataFieldNotPresent;
import gov.loc.repository.bagit.exceptions.conformance.RequiredTagFileNotPresent;

public final class BagProfileChecker {

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
   * @throws MetatdataValueIsNotAcceptable if a metadata value is not in the list of acceptable values
   * @throws RequiredMetadataFieldNotPresent if a metadata field is not present but it should be
   * @throws RequiredManifestNotPresent if a payload or tag manifest type is not present but should be
   * @throws BagitVersionIsNotAcceptable if the version of the bag is not in the list of acceptable versions
   * @throws RequiredTagFileNotPresent if a tag file is not present but should be
   */
  public static void bagConformsToProfile(final InputStream jsonProfile, final Bag bag) throws JsonParseException, JsonMappingException, 
  IOException, FetchFileNotAllowedException, RequiredMetadataFieldNotPresent, MetatdataValueIsNotAcceptable, RequiredManifestNotPresent, 
  BagitVersionIsNotAcceptable, RequiredTagFileNotPresent{
    
    final BagitProfile profile = parseBagitProfile(jsonProfile);
    checkFetch(bag.getRootDir(), profile.isAllowFetchFile(), bag.getItemsToFetch());
    
    checkMetadata(bag.getMetadata(), profile.getBagInfoEntryRequirements());
    
    requiredManifestsExist(bag.getPayLoadManifests(), profile.getManifestTypesRequired(), true);

    requiredManifestsExist(bag.getTagManifests(), profile.getTagManifestsRequired(), false);

    if(!profile.getAcceptableBagitVersions().contains(bag.getVersion().toString())){
      throw new BagitVersionIsNotAcceptable("Version [" + bag.getVersion().toString() + "] is not in the acceptable list of " + 
          profile.getAcceptableBagitVersions());
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
    if(!allowFetchFile && itemsToFetch.isEmpty()){
      throw new FetchFileNotAllowedException("Fetch File was found in bag [" + rootDir + "]");
    }
  }
  
  private static void checkMetadata(final List<SimpleImmutableEntry<String, String>> bagMetadata, 
      final Map<String, BagInfoEntry> bagInfoEntryRequirements) throws RequiredMetadataFieldNotPresent, MetatdataValueIsNotAcceptable{
    final MapOfLists metadataMap = convertMetadata(bagMetadata);
    
    for(final Entry<String, BagInfoEntry> bagInfoEntryRequirement : bagInfoEntryRequirements.entrySet()){
      final boolean metadataContainsKey = metadataMap.keySet().contains(bagInfoEntryRequirement.getKey());
      
      //is it required and not there?
      if(bagInfoEntryRequirement.getValue().isRequired() && !metadataContainsKey){
        throw new RequiredMetadataFieldNotPresent("Profile specifies metadata field [" + bagInfoEntryRequirement.getKey() + "] is required but was not found!");
      }
      
      //a size of zero implies that all values are acceptable
      if(bagInfoEntryRequirement.getValue().getAcceptableValues().size() > 0){
        //if it is present, and only certain values are allowed, check all the values to make sure they conform
        for(final String metadataValue : metadataMap.get(bagInfoEntryRequirement.getKey())){
          if(!bagInfoEntryRequirement.getValue().getAcceptableValues().contains(metadataValue)){
            throw new MetatdataValueIsNotAcceptable("Profile specifies that acceptable values for [" + bagInfoEntryRequirement.getKey() + 
                "] are " + bagInfoEntryRequirement.getValue().getAcceptableValues() + " but found [" + metadataValue + "]");
          }
        }
      }
    }
  }
  
  private static MapOfLists convertMetadata(final List<SimpleImmutableEntry<String, String>> bagMetadata){
    final MapOfLists metadataMap = new MapOfLists();
    
    //transform into a map so that we don't have to loop over the list every time since we don't care about order
    for(final SimpleImmutableEntry<String, String> metadata : bagMetadata){
      metadataMap.put(metadata.getKey(), metadata.getKey());
    }
    
    return metadataMap;
  }
  
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static void requiredManifestsExist(final Set<Manifest> manifests, final List<String> requiredManifestTypes, final boolean isPayloadManifest) throws RequiredManifestNotPresent{
    final Set<String> manifestTypesPresent = new HashSet<>();
    
    for(final Manifest manifest : manifests){
      manifestTypesPresent.add(manifest.getAlgorithm().getBagitName());
    }
    
    for(final String requiredManifestType : requiredManifestTypes){
      if(!manifestTypesPresent.contains(requiredManifestType)){
        final StringBuilder sb = new StringBuilder(25);
        sb.append("Required ");
        if(isPayloadManifest){ sb.append("tag");}
        sb.append("manifest type [").append(requiredManifestType).append("] was not present");
          
        throw new RequiredManifestNotPresent(sb.toString());
      }
    }
  }
  
  private static void requiredTagFilesExist(final Path rootDir, final List<String> requiredTagFilePaths) throws RequiredTagFileNotPresent{
    Path requiredTagFile;
    for(final String requiredTagFilePath : requiredTagFilePaths){
      requiredTagFile = rootDir.resolve(requiredTagFilePath);
      if(!Files.exists(requiredTagFile)){
        throw new RequiredTagFileNotPresent("Required tag file [" + requiredTagFilePath + "] was not found");
      }
    }
  }
  
  private static class MapOfLists extends HashMap<String, List<String>>{
    private static final long serialVersionUID = 1L;

    public void put(final String key, final String value){
      final List<String> values = this.get(key);
      final List<String> newValues = new ArrayList<>();
      
      if(values != null){
        newValues.addAll(values);
      }
      newValues.add(value);
      
      this.put(key, newValues);
    }
  }
}
