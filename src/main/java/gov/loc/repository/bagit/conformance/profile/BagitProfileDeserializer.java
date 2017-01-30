package gov.loc.repository.bagit.conformance.profile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
    
    final JsonNode bagitProfileInfoNode = node.get("BagIt-Profile-Info");
    final BagitProfileMetadata bagitProfileInfo = parseBagitProfileInfo(bagitProfileInfoNode);
    profile.setBagitProfileMetadata(bagitProfileInfo);
    
    final JsonNode bagInfoNode = node.get("Bag-Info");
    final Map<String, BagInfoEntry> bagInfo = parseBagInfo(bagInfoNode);
    profile.setBagInfoEntryRequirements(bagInfo);
    
    final JsonNode manifestRequiredNode = node.get("Manifests-Required");
    for (final JsonNode manifestName : manifestRequiredNode) {
      profile.getManifestTypesRequired().add(manifestName.asText());
    }
    
    profile.setAllowFetchFile(node.get("Allow-Fetch.txt").asBoolean());
    
    profile.setSerialization(Serialization.valueOf(node.get("Serialization").asText()));
    
    final JsonNode acceptableSerializationFormatsNodes = node.get("Accept-Serialization");
    for (final JsonNode acceptableSerializationFormatsNode : acceptableSerializationFormatsNodes) {
      profile.getAcceptableMIMESerializationTypes().add(acceptableSerializationFormatsNode.asText());
    }
    
    final JsonNode tagManifestsRequiredNodes = node.get("Tag-Manifests-Required");
    for(final JsonNode tagManifestsRequiredNode : tagManifestsRequiredNodes){
      profile.getTagManifestsRequired().add(tagManifestsRequiredNode.asText());
    }
    
    final JsonNode tagFilesRequiredNodes = node.get("Tag-Files-Required");
    for(final JsonNode tagFilesRequiredNode : tagFilesRequiredNodes){
      profile.getTagFilesRequired().add(tagFilesRequiredNode.asText());
    }
    
    final JsonNode acceptableVersionsNodes = node.get("Accept-BagIt-Version");
    for(final JsonNode acceptableVersionsNode : acceptableVersionsNodes){
      profile.getAcceptableBagitVersions().add(acceptableVersionsNode.asText());
    }
    
    return profile;
  }
  
  private BagitProfileMetadata parseBagitProfileInfo(final JsonNode bagitProfileInfoNode){
    final BagitProfileMetadata bagitProfileInfo = new BagitProfileMetadata();
    
    bagitProfileInfo.setBagitProfileIdentifier(bagitProfileInfoNode.get("BagIt-Profile-Identifier").asText());
    bagitProfileInfo.setSourceOrganization(bagitProfileInfoNode.get("Source-Organization").asText());
    bagitProfileInfo.setContactName(bagitProfileInfoNode.get("Contact-Name").asText());
    bagitProfileInfo.setContactEmail(bagitProfileInfoNode.get("Contact-Email").asText());
    bagitProfileInfo.setExternalDescription(bagitProfileInfoNode.get("External-Description").asText());
    bagitProfileInfo.setVersion(bagitProfileInfoNode.get("Version").asText());
    
    return bagitProfileInfo;
  }
  
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private Map<String, BagInfoEntry> parseBagInfo(final JsonNode bagInfoNode){
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
      
      bagInfo.put(node.getKey(), entry);
    }
    
    return bagInfo;
  }
}
