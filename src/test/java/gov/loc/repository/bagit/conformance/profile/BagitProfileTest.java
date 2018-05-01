package gov.loc.repository.bagit.conformance.profile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BagitProfileTest extends AbstractBagitProfileTest{
  
  @Test
  public void testToString() throws Exception{
    String expectedOutput = "BagitProfile [bagitProfileIdentifier=http://canadiana.org/standards/bagit/tdr_ingest.json, "
        + "sourceOrganization=Candiana.org, "
        + "externalDescription=BagIt profile for ingesting content into the C.O. TDR loading dock., "
        + "contactName=William Wueppelmann, "
        + "contactEmail=tdr@canadiana.com, "
        + "version=1.2, "
        + "bagInfoRequirements={"
        + "Payload-Oxum=[required=true, acceptableValues=[], repeatable=false], "
        + "Bag-Size=[required=true, acceptableValues=[], repeatable=false], "
        + "Bagging-Date=[required=true, acceptableValues=[], repeatable=false], "
        + "Source-Organization=[required=true, acceptableValues=[Simon Fraser University, York University], repeatable=false], "
        + "Bag-Count=[required=true, acceptableValues=[], repeatable=false], "
        + "Organization-Address=[required=true, acceptableValues=[8888 University Drive Burnaby, B.C. V5A 1S6 Canada, 4700 Keele Street Toronto, Ontario M3J 1P3 Canada], repeatable=false], "
        + "Bag-Group-Identifier=[required=false, acceptableValues=[], repeatable=false], "
        + "External-Identifier=[required=false, acceptableValues=[], repeatable=false], "
        + "Internal-Sender-Identifier=[required=false, acceptableValues=[], repeatable=false], "
        + "Contact-Email=[required=true, acceptableValues=[], repeatable=false], "
        + "Contact-Phone=[required=false, acceptableValues=[], repeatable=false], "
        + "Internal-Sender-Description=[required=false, acceptableValues=[], repeatable=false], "
        + "External-Description=[required=true, acceptableValues=[], repeatable=false], "
        + "Contact-Name=[required=true, acceptableValues=[Mark Jordan, Nick Ruest], repeatable=false]}, "
        + "manifestTypesRequired=[md5], "
        + "fetchFileAllowed=false, "
        + "serialization=forbidden, "
        + "acceptableMIMESerializationTypes=[application/zip], "
        + "acceptableBagitVersions=[0.96], "
        + "tagManifestTypesRequired=[md5], "
        + "tagFilesRequired=[DPN/dpnFirstNode.txt, DPN/dpnRegistry]]";
    
    BagitProfile profile = mapper.readValue(new File("src/test/resources/bagitProfiles/exampleProfile.json"), BagitProfile.class);
    System.err.println(profile.toString());
    Assertions.assertEquals(expectedOutput, profile.toString());
  }
  
  @Test
  public void testEquals(){
    BagitProfile profile = createExpectedProfile();
    
    Assertions.assertFalse(profile.equals(null));
    
    BagitProfile differentBagitProfileIdentifier = createExpectedProfile();
    differentBagitProfileIdentifier.setBagitProfileIdentifier("foo");
    Assertions.assertFalse(profile.equals(differentBagitProfileIdentifier));
    
    BagitProfile differentSourceOrganization = createExpectedProfile();
    differentSourceOrganization.setSourceOrganization("foo");
    Assertions.assertFalse(profile.equals(differentSourceOrganization));
    
    BagitProfile differentExternalDescription = createExpectedProfile();
    differentExternalDescription.setExternalDescription("foo");
    Assertions.assertFalse(profile.equals(differentExternalDescription));
    
    BagitProfile differentContactName = createExpectedProfile();
    differentContactName.setContactName("foo");
    Assertions.assertFalse(profile.equals(differentContactName));
    
    BagitProfile differentContactEmail = createExpectedProfile();
    differentContactEmail.setContactEmail("foo");
    Assertions.assertFalse(profile.equals(differentContactEmail));
    
    BagitProfile differentVersion = createExpectedProfile();
    differentVersion.setVersion("foo");
    Assertions.assertFalse(profile.equals(differentVersion));
    
    BagitProfile differentBagInfoRequirements = createExpectedProfile();
    differentBagInfoRequirements.setBagInfoRequirements(new HashMap<>());
    Assertions.assertFalse(profile.equals(differentBagInfoRequirements));
    
    BagitProfile differentManifestTypesRequired = createExpectedProfile();
    differentManifestTypesRequired.setManifestTypesRequired(Arrays.asList("foo"));
    Assertions.assertFalse(profile.equals(differentManifestTypesRequired));
    
    BagitProfile differentFetchFileAllowed = createExpectedProfile();
    differentFetchFileAllowed.setFetchFileAllowed(true);
    Assertions.assertFalse(profile.equals(differentFetchFileAllowed));
    
    BagitProfile differentSerialization = createExpectedProfile();
    differentSerialization.setSerialization(Serialization.required);
    Assertions.assertFalse(profile.equals(differentSerialization));
    
    BagitProfile differentAcceptableMIMESerializationTypes = createExpectedProfile();
    differentAcceptableMIMESerializationTypes.setAcceptableMIMESerializationTypes(Arrays.asList("foo"));
    Assertions.assertFalse(profile.equals(differentAcceptableMIMESerializationTypes));
    
    BagitProfile differentAcceptableBagitVersions = createExpectedProfile();
    differentAcceptableBagitVersions.setAcceptableBagitVersions(Arrays.asList("foo"));
    Assertions.assertFalse(profile.equals(differentAcceptableBagitVersions));
    
    BagitProfile differentTagManifestTypesRequired = createExpectedProfile();
    differentTagManifestTypesRequired.setTagManifestTypesRequired(Arrays.asList("foo"));
    Assertions.assertFalse(profile.equals(differentTagManifestTypesRequired));
    
    BagitProfile differentTagFilesRequired = createExpectedProfile();
    differentTagFilesRequired.setTagFilesRequired(Arrays.asList("foo"));
    Assertions.assertFalse(profile.equals(differentTagFilesRequired));
  }
}
