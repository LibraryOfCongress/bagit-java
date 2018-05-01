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
    Assertions.assertEquals(expectedProfile.getExternalDescription(), profile.getExternalDescription());
    Assertions.assertEquals(expectedProfile.getManifestTypesRequired(), profile.getManifestTypesRequired());
    Assertions.assertEquals(expectedProfile.getSerialization(), profile.getSerialization());
    Assertions.assertEquals(expectedProfile.getSourceOrganization(), profile.getSourceOrganization());
    Assertions.assertEquals(expectedProfile.getTagFilesRequired(), profile.getTagFilesRequired());
    Assertions.assertEquals(expectedProfile.getTagManifestTypesRequired(), profile.getTagManifestTypesRequired());
    Assertions.assertEquals(expectedProfile.getVersion(), profile.getVersion());
    Assertions.assertEquals(expectedProfile.hashCode(), profile.hashCode());
  }
  
  
}
