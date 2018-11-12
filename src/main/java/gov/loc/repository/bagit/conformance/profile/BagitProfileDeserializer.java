package gov.loc.repository.bagit.conformance.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

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
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");

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

    parseBagitProfileInfo(node, profile);

    profile.setBagInfoRequirements(parseBagInfo(node));

    profile.getManifestTypesRequired().addAll(parseManifestTypesRequired(node));

    profile.setFetchFileAllowed(node.get("Allow-Fetch.txt").asBoolean());
    logger.debug(messages.getString("fetch_allowed"), profile.isFetchFileAllowed());

    profile.setSerialization(Serialization.valueOf(node.get("Serialization").asText()));
    logger.debug(messages.getString("serialization_allowed"), profile.getSerialization());

    profile.getAcceptableMIMESerializationTypes().addAll(parseAcceptableSerializationFormats(node));

    profile.getTagManifestTypesRequired().addAll(parseRequiredTagmanifestTypes(node));

    profile.getTagFilesRequired().addAll(parseRequiredTagFiles(node));

    profile.getAcceptableBagitVersions().addAll(parseAcceptableVersions(node));

    return profile;
  }

  private static void parseBagitProfileInfo(final JsonNode node, final BagitProfile profile) {
    final JsonNode bagitProfileInfoNode = node.get("BagIt-Profile-Info");
    logger.debug(messages.getString("parsing_bagit_profile_info_section"));

    // Read required tags first
    // due to specification defined at https://github.com/bagit-profiles/bagit-profiles
    final String profileIdentifier = bagitProfileInfoNode.get("BagIt-Profile-Identifier").asText();
    logger.debug(messages.getString("identifier"), profileIdentifier);
    profile.setBagitProfileIdentifier(profileIdentifier);

    final String sourceOrg = bagitProfileInfoNode.get("Source-Organization").asText();
    logger.debug(messages.getString("source_organization"), sourceOrg);
    profile.setSourceOrganization(sourceOrg);

    final String extDescript = bagitProfileInfoNode.get("External-Description").asText();
    logger.debug(messages.getString("external_description"), extDescript);
    profile.setExternalDescription(extDescript);

    final String version = bagitProfileInfoNode.get("Version").asText();
    logger.debug(messages.getString("version"), version);
    profile.setVersion(version);

    final JsonNode contactNameNode = bagitProfileInfoNode.get("Contact-Name");
    if (contactNameNode != null) {
      final String contactName = contactNameNode.asText();
      logger.debug(messages.getString("contact_name"), contactName);
      profile.setContactName(contactName);
    }

    final JsonNode contactEmailNode = bagitProfileInfoNode.get("Contact-Email");
    if (contactEmailNode != null) {
      final String contactEmail = contactEmailNode.asText();
      logger.debug(messages.getString("contact_email"), contactEmail);
      profile.setContactEmail(contactEmail);
    }

    final JsonNode contactPhoneNode = bagitProfileInfoNode.get("Contact-Phone");
    if (contactPhoneNode != null) {
      final String contactPhone = contactPhoneNode.asText();
      logger.debug(messages.getString("contact_phone"), contactPhone);
      profile.setContactPhone(contactPhone);
    }
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static Map<String, BagInfoRequirement> parseBagInfo(final JsonNode rootNode) {
    final JsonNode bagInfoNode = rootNode.get("Bag-Info");
    logger.debug(messages.getString("parsing_bag_info"));
    final Map<String, BagInfoRequirement> bagInfo = new HashMap<>();

    final Iterator<Entry<String, JsonNode>> nodes = bagInfoNode.fields(); //stuck in java 6...

    while (nodes.hasNext()) {
      final Entry<String, JsonNode> node = nodes.next();

      final BagInfoRequirement entry = new BagInfoRequirement();
      // due to specification required is false by default.
      final JsonNode requiredNode = node.getValue().get("required");
      if (requiredNode != null) {
        entry.setRequired(requiredNode.asBoolean());
      }

      final JsonNode valuesNode = node.getValue().get("values");
      if (valuesNode != null) {
        for (final JsonNode value : valuesNode) {
          entry.getAcceptableValues().add(value.asText());
        }
      }

      final JsonNode repeatableNode = node.getValue().get("repeatable");
      if (repeatableNode != null) {
        entry.setRepeatable(repeatableNode.asBoolean());
      }

      logger.debug("{}: {}", node.getKey(), entry);
      bagInfo.put(node.getKey(), entry);
    }

    return bagInfo;
  }

  private static List<String> parseManifestTypesRequired(final JsonNode node) {
    final JsonNode manifests = node.get("Manifests-Required");

    final List<String> manifestTypes = new ArrayList<>();

    for (final JsonNode manifestName : manifests) {
      manifestTypes.add(manifestName.asText());
    }

    logger.debug(messages.getString("required_manifest_types"), manifestTypes);

    return manifestTypes;
  }

  private static List<String> parseAcceptableSerializationFormats(final JsonNode node) {
    final JsonNode serialiationFormats = node.get("Accept-Serialization");
    final List<String> serialTypes = new ArrayList<>();

    for (final JsonNode serialiationFormat : serialiationFormats) {
      serialTypes.add(serialiationFormat.asText());
    }
    logger.debug(messages.getString("acceptable_serialization_mime_types"), serialTypes);

    return serialTypes;
  }

  private static List<String> parseRequiredTagmanifestTypes(final JsonNode node) {
    final JsonNode tagManifestsRequiredNodes = node.get("Tag-Manifests-Required");
    final List<String> requiredTagmanifestTypes = new ArrayList<>();
    if (tagManifestsRequiredNodes != null) {
      for (final JsonNode tagManifestsRequiredNode : tagManifestsRequiredNodes) {
        requiredTagmanifestTypes.add(tagManifestsRequiredNode.asText());
      }
    }
    logger.debug(messages.getString("required_tagmanifest_types"), requiredTagmanifestTypes);

    return requiredTagmanifestTypes;
  }

  private static List<String> parseRequiredTagFiles(final JsonNode node) {
    final JsonNode tagFilesRequiredNodes = node.get("Tag-Files-Required");
    final List<String> requiredTagFiles = new ArrayList<>();

    for (final JsonNode tagFilesRequiredNode : tagFilesRequiredNodes) {
      requiredTagFiles.add(tagFilesRequiredNode.asText());
    }
    logger.debug(messages.getString("tag_files_required"), requiredTagFiles);

    return requiredTagFiles;
  }

  private static List<String> parseAcceptableVersions(final JsonNode node) {
    final JsonNode acceptableVersionsNodes = node.get("Accept-BagIt-Version");
    final List<String> acceptableVersions = new ArrayList<>();

    for (final JsonNode acceptableVersionsNode : acceptableVersionsNodes) {
      acceptableVersions.add(acceptableVersionsNode.asText());
    }
    logger.debug(messages.getString("acceptable_bagit_versions"), acceptableVersions);

    return acceptableVersions;
  }
}
