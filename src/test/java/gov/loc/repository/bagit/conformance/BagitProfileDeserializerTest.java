package gov.loc.repository.bagit.conformance;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import gov.loc.repository.bagit.conformance.profile.BagInfoEntry;
import gov.loc.repository.bagit.conformance.profile.BagitProfile;
import gov.loc.repository.bagit.conformance.profile.BagitProfileDeserializer;
import gov.loc.repository.bagit.conformance.profile.BagitProfileMetadata;
import gov.loc.repository.bagit.conformance.profile.Serialization;

public class BagitProfileDeserializerTest extends Assert{

  @Test
  public void testDeserialize() throws Exception{
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(BagitProfile.class, new BagitProfileDeserializer());
    mapper.registerModule(module);

    BagitProfile expectedProfile = createExpectedProfile();
    
    BagitProfile profile = mapper.readValue(new File("src/test/resources/bagitProfiles/exampleProfile.json"), BagitProfile.class);
    
    assertEquals(expectedProfile, profile);
  }
  
  private BagitProfile createExpectedProfile(){
    BagitProfile expectedProfile = new BagitProfile();
    
    BagitProfileMetadata bagitProfileInfo = new BagitProfileMetadata();
    bagitProfileInfo.setBagitProfileIdentifier("http://canadiana.org/standards/bagit/tdr_ingest.json");
    bagitProfileInfo.setContactEmail("tdr@canadiana.com");
    bagitProfileInfo.setContactName("William Wueppelmann");
    bagitProfileInfo.setExternalDescription("BagIt profile for ingesting content into the C.O. TDR loading dock.");
    bagitProfileInfo.setSourceOrganization("Candiana.org");
    bagitProfileInfo.setVersion("1.2");
    expectedProfile.setBagitProfileMetadata(bagitProfileInfo);
    
    expectedProfile.setBagInfoEntryRequirements(createBagInfo());
    
    expectedProfile.setManifestTypesRequired(Arrays.asList("md5"));
    
    expectedProfile.setFetchFileAllowed(false);
    
    expectedProfile.setSerialization(Serialization.forbidden);
    
    expectedProfile.setAcceptableMIMESerializationTypes(Arrays.asList("application/zip"));
    
    expectedProfile.setAcceptableBagitVersions(Arrays.asList("0.96"));
    
    expectedProfile.setTagManifestsRequired(Arrays.asList("md5"));
    
    expectedProfile.setTagFilesRequired(Arrays.asList("DPN/dpnFirstNode.txt", "DPN/dpnRegistry"));
    
    return expectedProfile;
  }
  
  private Map<String, BagInfoEntry> createBagInfo(){
    Map<String, BagInfoEntry> info = new HashMap<>();
    
    info.put("Source-Organization", new BagInfoEntry(true, Arrays.asList("Simon Fraser University", "York University")));
    info.put("Organization-Address", new BagInfoEntry(true, 
        Arrays.asList("8888 University Drive Burnaby, B.C. V5A 1S6 Canada", "4700 Keele Street Toronto, Ontario M3J 1P3 Canada")));
    info.put("Contact-Name", new BagInfoEntry(true, Arrays.asList("Mark Jordan", "Nick Ruest")));
    info.put("Contact-Phone", new BagInfoEntry(false, Arrays.asList()));
    info.put("Contact-Email", new BagInfoEntry(true, Arrays.asList()));
    info.put("External-Description", new BagInfoEntry(true, Arrays.asList()));
    info.put("External-Identifier", new BagInfoEntry(false, Arrays.asList()));
    info.put("Bag-Size", new BagInfoEntry(true, Arrays.asList()));
    info.put("Bag-Group-Identifier", new BagInfoEntry(false, Arrays.asList()));
    info.put("Bag-Count", new BagInfoEntry(true, Arrays.asList()));
    info.put("Internal-Sender-Identifier", new BagInfoEntry(false, Arrays.asList()));
    info.put("Internal-Sender-Description", new BagInfoEntry(false, Arrays.asList()));
    info.put("Bagging-Date", new BagInfoEntry(true, Arrays.asList()));
    info.put("Payload-Oxum", new BagInfoEntry(true, Arrays.asList()));

    return info;
  }
}
