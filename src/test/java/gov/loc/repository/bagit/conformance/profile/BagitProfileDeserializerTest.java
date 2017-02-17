package gov.loc.repository.bagit.conformance.profile;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BagitProfileDeserializerTest extends AbstractBagitProfileTest{
  private ObjectMapper mapper;
  
  @Before
  public void setup(){
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(BagitProfile.class, new BagitProfileDeserializer());
    mapper.registerModule(module);
  }

  @Test
  public void testDeserialize() throws Exception{
    BagitProfile expectedProfile = createExpectedProfile();
    
    BagitProfile profile = mapper.readValue(new File("src/test/resources/bagitProfiles/exampleProfile.json"), BagitProfile.class);
    
    assertEquals(expectedProfile, profile);
    assertEquals(expectedProfile.getAcceptableBagitVersions(), profile.getAcceptableBagitVersions());
    assertEquals(expectedProfile.getAcceptableMIMESerializationTypes(), profile.getAcceptableMIMESerializationTypes());
    assertEquals(expectedProfile.getBagInfoRequirements(), profile.getBagInfoRequirements());
    assertEquals(expectedProfile.getBagitProfileIdentifier(), profile.getBagitProfileIdentifier());
    assertEquals(expectedProfile.getContactEmail(), profile.getContactEmail());
    assertEquals(expectedProfile.getContactName(), profile.getContactName());
    assertEquals(expectedProfile.getExternalDescription(), profile.getExternalDescription());
    assertEquals(expectedProfile.getManifestTypesRequired(), profile.getManifestTypesRequired());
    assertEquals(expectedProfile.getSerialization(), profile.getSerialization());
    assertEquals(expectedProfile.getSourceOrganization(), profile.getSourceOrganization());
    assertEquals(expectedProfile.getTagFilesRequired(), profile.getTagFilesRequired());
    assertEquals(expectedProfile.getTagManifestTypesRequired(), profile.getTagManifestTypesRequired());
    assertEquals(expectedProfile.getVersion(), profile.getVersion());
    assertEquals(expectedProfile.hashCode(), profile.hashCode());
  }
  
  
}
