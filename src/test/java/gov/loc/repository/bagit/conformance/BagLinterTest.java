package gov.loc.repository.bagit.conformance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.loc.repository.bagit.conformance.profile.BagitProfile;
import gov.loc.repository.bagit.conformance.profile.Serialization;

public class BagLinterTest extends Assert{
  
  private BagLinter sut = new BagLinter();
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testLintBag() throws Exception{
    Set<BagitWarning> expectedWarnings = new HashSet<>();
    expectedWarnings.addAll(Arrays.asList(BagitWarning.values()));
    Set<BagitWarning> warnings = sut.lintBag(rootDir);

    if(System.getProperty("os.name").equals("Mac OS X")){
      expectedWarnings.remove(BagitWarning.DIFFERENT_NORMALIZATION); //don't test normalization on mac
    }
    
    Set<BagitWarning> diff = new HashSet<>(expectedWarnings);
    diff.removeAll(warnings);
    
    assertEquals("Warnings missing: " + diff.toString() + "\n", expectedWarnings, warnings);
  }
  
  @Test
  public void foo() throws IOException{
    File file = new File("src/test/resources/bagitProfiles/exampleProfile.json");
    ObjectMapper mapper = new ObjectMapper();
    BagitProfile jsonObject = mapper.readValue(file, BagitProfile.class);
    
    assertEquals(Arrays.asList("0.96"), jsonObject.getAcceptBagitVersion());
    assertEquals(Arrays.asList("application/zip"), jsonObject.getAcceptSerialization());
    
    //TODO assertEquals(Serialization.optional, jsonObject.getBagInfo());
    
    assertEquals("http://canadiana.org/standards/bagit/tdr_ingest.json", jsonObject.getBagitProfileInfo().getBagitProfileIdentifier());
    assertEquals("tdr@canadiana.com", jsonObject.getBagitProfileInfo().getContactEmail());
    assertEquals("William Wueppelmann", jsonObject.getBagitProfileInfo().getContactName());
    assertEquals("BagIt profile for ingesting content into the C.O. TDR loading dock.", jsonObject.getBagitProfileInfo().getExternalDescription());
    assertEquals("Candiana.org", jsonObject.getBagitProfileInfo().getSourceOrganization());
    assertEquals("1.2", jsonObject.getBagitProfileInfo().getVersion());
    
    assertEquals(Arrays.asList("md5"), jsonObject.getManifestsRequired());
    assertEquals(Serialization.optional, jsonObject.getSerialization());
    assertEquals(Arrays.asList("DPN/dpnFirstNode.txt", "DPN/dpnRegistry"), jsonObject.getTagFilesRequired());
    assertEquals(Arrays.asList("md5"), jsonObject.getTagManifestsRequired());
    
    System.err.println(Arrays.toString(jsonObject.getBagInfo().getClass().getDeclaredFields()));
    
//    Path path = new File("src/test/resources/bagitProfiles/exampleProfile.json").toPath();
//    String jsonProfile = new String(Files.readAllBytes(path));
    
    
  }
}