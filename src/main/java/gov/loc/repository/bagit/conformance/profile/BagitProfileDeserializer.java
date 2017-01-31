package gov.loc.repository.bagit.conformance.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserialize bagit profile json to a {@link BagitProfile} 
 */
public class BagitProfileDeserializer extends StdDeserializer<BagitProfile> {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = LoggerFactory.getLogger(BagitProfileDeserializer.class);

  public BagitProfileDeserializer() {
    this(null);
  }

  public BagitProfileDeserializer(final Class<?> vc) {
    super(vc);
  }

  @Override
  public BagitProfile deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    final BagitProfile profile = new BagitProfile();
    final JsonNode node = p.getCodec().readTree(p);
    
    final BagitProfileMetadata bagitProfileInfo = parseBagitProfileInfo(node);
    profile.setBagitProfileMetadata(bagitProfileInfo);
    
    profile.setBagInfoEntryRequirements(parseBagInfo(node));
    
    profile.getManifestTypesRequired().addAll(parseManifestTypesRequired(node));
    
    profile.setFetchFileAllowed(node.get("Allow-Fetch.txt").asBoolean());
    logger.debug("Are fetch files allowed? {}", profile.isFetchFileAllowed());
    
    profile.setSerialization(Serialization.valueOf(node.get("Serialization").asText()));
    logger.debug("Serialization allowed [{}]",profile.getSerialization());
    
    profile.getAcceptableMIMESerializationTypes().addAll(parseAcceptableSerializationFormats(node));
    
    profile.getTagManifestsRequired().addAll(parseRequiredTagmanifestTypes(node));
    
    profile.getTagFilesRequired().addAll(parseRequiredTagFiles(node));
    
    profile.getAcceptableBagitVersions().addAll(parseAcceptableVersions(node));
    
    return profile;
  }
  
  private BagitProfileMetadata parseBagitProfileInfo(final JsonNode node){
    final JsonNode bagitProfileInfoNode = node.get("BagIt-Profile-Info");
    
    logger.debug("Parsing the BagIt-Profile-Info section");
    final BagitProfileMetadata bagitProfileInfo = new BagitProfileMetadata();
    
    final String profileIdentifier = bagitProfileInfoNode.get("BagIt-Profile-Identifier").asText();
    logger.debug("Identifier is [{}]", profileIdentifier);
    bagitProfileInfo.setBagitProfileIdentifier(profileIdentifier);
    
    final String sourceOrg = bagitProfileInfoNode.get("Source-Organization").asText();
    logger.debug("Source-Organization is [{}]", sourceOrg);
    bagitProfileInfo.setSourceOrganization(sourceOrg);
    
    final String contactName = bagitProfileInfoNode.get("Contact-Name").asText();
    logger.debug("Contact-Name is [{}]", contactName);
    bagitProfileInfo.setContactName(contactName);
    
    final String contactEmail = bagitProfileInfoNode.get("Contact-Email").asText();
    logger.debug("Contact-Email is [{}]", contactEmail);
    bagitProfileInfo.setContactEmail(contactEmail);
    
    final String extDescript = bagitProfileInfoNode.get("External-Description").asText();
    logger.debug("External-Description is [{}]", extDescript);
    bagitProfileInfo.setExternalDescription(extDescript);
    
    final String version = bagitProfileInfoNode.get("Version").asText();
    logger.debug("Version is [{}]", version);
    bagitProfileInfo.setVersion(version);
    
    return bagitProfileInfo;
  }
  
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private Map<String, BagInfoEntry> parseBagInfo(final JsonNode rootNode){
    final JsonNode bagInfoNode = rootNode.get("Bag-Info");
    logger.debug("Parsing the Bag-Info section");
    final Map<String, BagInfoEntry>  bagInfo = new HashMap<>();
    
    final Iterator<Entry<String, JsonNode>> nodes = bagInfoNode.fields(); //stuck in java 6...
    
    while(nodes.hasNext()){
      final Entry<String, JsonNode> node = nodes.next();
      
      final BagInfoEntry entry = new BagInfoEntry();
      entry.setRequired(node.getValue().get("required").asBoolean());
      
      final JsonNode valuesNode = node.getValue().get("values");
      if(valuesNode != null){
        for(final JsonNode value : valuesNode){
          entry.getAcceptableValues().add(value.asText());
        }
      }
      
      logger.debug("{}: {}", node.getKey(), entry);
      bagInfo.put(node.getKey(), entry);
    }
    
    return bagInfo;
  }
  
  private List<String> parseManifestTypesRequired(final JsonNode node){
    final JsonNode manifests = node.get("Manifests-Required");
    
    final List<String> manifestTypes = new ArrayList<>();
    
    for (final JsonNode manifestName : manifests) {
      manifestTypes.add(manifestName.asText());
    }
    
    logger.debug("Required manifest types {}", manifestTypes);
    
    return manifestTypes;
  }
  
  private List<String> parseAcceptableSerializationFormats(final JsonNode node){
    final JsonNode serialiationFormats = node.get("Accept-Serialization");
    final List<String> serialTypes = new ArrayList<>();
    
    for (final JsonNode serialiationFormat : serialiationFormats) {
      serialTypes.add(serialiationFormat.asText());
    }
    logger.debug("Acceptable serialization MIME types are {}", serialTypes);
    
    return serialTypes;
  }
  
  private List<String> parseRequiredTagmanifestTypes(final JsonNode node){
    final JsonNode tagManifestsRequiredNodes = node.get("Tag-Manifests-Required");
    final List<String> requiredTagmanifestTypes = new ArrayList<>();
    
    for(final JsonNode tagManifestsRequiredNode : tagManifestsRequiredNodes){
      requiredTagmanifestTypes.add(tagManifestsRequiredNode.asText());
    }
    logger.debug("Required Tagmanifest types are {}", requiredTagmanifestTypes);
    
    return requiredTagmanifestTypes;
  }
  
  private List<String> parseRequiredTagFiles(final JsonNode node){
    final JsonNode tagFilesRequiredNodes = node.get("Tag-Files-Required");
    final List<String> requiredTagFiles = new ArrayList<>();
    
    for(final JsonNode tagFilesRequiredNode : tagFilesRequiredNodes){
      requiredTagFiles.add(tagFilesRequiredNode.asText());
    }
    logger.debug("Tag files required are {}", requiredTagFiles);
    
    return requiredTagFiles;
  }
  
  private List<String> parseAcceptableVersions(final JsonNode node){
    final JsonNode acceptableVersionsNodes = node.get("Accept-BagIt-Version");
    final List<String> acceptableVersions = new ArrayList<>();
    
    for(final JsonNode acceptableVersionsNode : acceptableVersionsNodes){
      acceptableVersions.add(acceptableVersionsNode.asText());
    }
    logger.debug("Acceptable bagit versions are {}", acceptableVersions);
    
    return acceptableVersions;
  }
}
