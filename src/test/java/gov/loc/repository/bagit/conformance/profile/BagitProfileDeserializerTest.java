package gov.loc.repository.bagit.conformance.profile;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BagitProfileDeserializerTest extends AbstractBagitProfileTest{

  @Test
  public void testDeserialize() throws Exception{
    BagitProfile expectedProfile = createExpectedProfile();
    
    BagitProfile profile = mapper.readValue(new File("src/test/resources/bagitProfiles/exampleProfile.json"), BagitProfile.class);
    
    Assertions.assertEquals(expectedProfile, profile);
    Assertions.assertEquals(expectedProfile.getAcceptableBagitVersions(), profile.getAcceptableBagitVersions());
    Assertions.assertEquals(expectedProfile.getAcceptableMIMESerializationTypes(), profile.getAcceptableMIMESerializationTypes());
    Assertions.assertEquals(expectedProfile.getBagInfoRequirements(), profile.getBagInfoRequirements());
    Assertions.assertEquals(expectedProfile.getBagitProfileIdentifier(), profile.getBagitProfileIdentifier());
    Assertions.assertEquals(expectedProfile.getContactEmail(), profile.getContactEmail());
    Assertions.assertEquals(expectedProfile.getContactName(), profile.getContactName());
    Assertions.assertEquals(expectedProfile.getContactPhone(), profile.getContactPhone());
    Assertions.assertEquals(expectedProfile.getExternalDescription(), profile.getExternalDescription());
    Assertions.assertEquals(expectedProfile.getManifestTypesRequired(), profile.getManifestTypesRequired());
    Assertions.assertEquals(expectedProfile.getSerialization(), profile.getSerialization());
    Assertions.assertEquals(expectedProfile.getSourceOrganization(), profile.getSourceOrganization());
    Assertions.assertEquals(expectedProfile.getTagFilesRequired(), profile.getTagFilesRequired());
    Assertions.assertEquals(expectedProfile.getTagManifestTypesRequired(), profile.getTagManifestTypesRequired());
    Assertions.assertEquals(expectedProfile.getVersion(), profile.getVersion());
    Assertions.assertEquals(expectedProfile.hashCode(), profile.hashCode());
  }
  
  @Test
  public void testDeserializeWithoutOptionalTags() throws Exception{
    BagitProfile minimalProfile = createMinimalProfile();
    
    BagitProfile profile = mapper.readValue(new File("src/test/resources/bagitProfiles/exampleProfileOnlyRequiredFields.json"), BagitProfile.class);
    System.err.println(minimalProfile.toString());
    System.err.println(profile.toString());
    Assertions.assertEquals(minimalProfile, profile);
    Assertions.assertEquals(minimalProfile.getAcceptableBagitVersions(), profile.getAcceptableBagitVersions());
    Assertions.assertEquals(minimalProfile.getAcceptableMIMESerializationTypes(), profile.getAcceptableMIMESerializationTypes());
    Assertions.assertEquals(minimalProfile.getBagInfoRequirements(), profile.getBagInfoRequirements());
    Assertions.assertEquals(minimalProfile.getBagitProfileIdentifier(), profile.getBagitProfileIdentifier());
    Assertions.assertEquals(minimalProfile.getContactEmail(), profile.getContactEmail());
    Assertions.assertEquals(minimalProfile.getContactName(), profile.getContactName());
    Assertions.assertEquals(minimalProfile.getContactPhone(), profile.getContactPhone());
    Assertions.assertEquals(minimalProfile.getExternalDescription(), profile.getExternalDescription());
    Assertions.assertEquals(minimalProfile.getManifestTypesRequired(), profile.getManifestTypesRequired());
    Assertions.assertEquals(minimalProfile.getSerialization(), profile.getSerialization());
    Assertions.assertEquals(minimalProfile.getSourceOrganization(), profile.getSourceOrganization());
    Assertions.assertEquals(minimalProfile.getTagFilesRequired(), profile.getTagFilesRequired());
    Assertions.assertEquals(minimalProfile.getTagManifestTypesRequired(), profile.getTagManifestTypesRequired());
    Assertions.assertEquals(minimalProfile.getVersion(), profile.getVersion());
    Assertions.assertEquals(minimalProfile.hashCode(), profile.hashCode());
  }
  
  
}
