/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.conformance.profile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class AbstractBagitProfileTest {
  protected ObjectMapper mapper;
  
  @BeforeEach
  public void setup(){
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(BagitProfile.class, new BagitProfileDeserializer());
    mapper.registerModule(module);
  }
  
  protected BagitProfile createExpectedProfile(){
    BagitProfile expectedProfile = new BagitProfile();
    
    expectedProfile.setBagitProfileIdentifier("http://canadiana.org/standards/bagit/tdr_ingest.json");
    expectedProfile.setContactEmail("tdr@canadiana.com");
    expectedProfile.setContactName("William Wueppelmann");
    expectedProfile.setContactPhone("+1 613 907 7040");
    expectedProfile.setExternalDescription("BagIt profile for ingesting content into the C.O. TDR loading dock.");
    expectedProfile.setSourceOrganization("Candiana.org");
    expectedProfile.setVersion("1.2");
    
    expectedProfile.setBagInfoRequirements(createBagInfo());
    expectedProfile.setManifestTypesRequired(Arrays.asList("md5"));
    expectedProfile.setFetchFileAllowed(false);
    expectedProfile.setSerialization(Serialization.forbidden);
    expectedProfile.setAcceptableMIMESerializationTypes(Arrays.asList("application/zip"));
    expectedProfile.setAcceptableBagitVersions(Arrays.asList("0.96"));
    expectedProfile.setTagManifestTypesRequired(Arrays.asList("md5"));
    expectedProfile.setTagFilesRequired(Arrays.asList("DPN/dpnFirstNode.txt", "DPN/dpnRegistry"));
    
    return expectedProfile;
  }
  
  protected BagitProfile createMinimalProfile(){
    BagitProfile expectedProfile = new BagitProfile();
    
    expectedProfile.setBagitProfileIdentifier("http://canadiana.org/standards/bagit/tdr_ingest.json");
    expectedProfile.setExternalDescription("BagIt profile for ingesting content into the C.O. TDR loading dock.");
    expectedProfile.setSourceOrganization("Candiana.org");
    expectedProfile.setVersion("1.2");
    
    expectedProfile.setBagInfoRequirements(createBagInfo());
    expectedProfile.setManifestTypesRequired(Arrays.asList("md5"));
    expectedProfile.setFetchFileAllowed(false);
    expectedProfile.setSerialization(Serialization.forbidden);
    expectedProfile.setAcceptableMIMESerializationTypes(Arrays.asList("application/zip"));
    expectedProfile.setAcceptableBagitVersions(Arrays.asList("0.96"));
    
    return expectedProfile;
  }
  
  protected Map<String, BagInfoRequirement> createBagInfo(){
    Map<String, BagInfoRequirement> info = new HashMap<>();
    
    info.put("Source-Organization", new BagInfoRequirement(true, Arrays.asList("Simon Fraser University", "York University"), false));
    info.put("Organization-Address", new BagInfoRequirement(true, 
        Arrays.asList("8888 University Drive Burnaby, B.C. V5A 1S6 Canada", "4700 Keele Street Toronto, Ontario M3J 1P3 Canada"), false));
    info.put("Contact-Name", new BagInfoRequirement(true, Arrays.asList("Mark Jordan", "Nick Ruest"), false));
    info.put("Contact-Phone", new BagInfoRequirement(false, Arrays.asList(), false));
    info.put("Contact-Email", new BagInfoRequirement(true, Arrays.asList(), false));
    info.put("External-Description", new BagInfoRequirement(true, Arrays.asList(), false));
    info.put("External-Identifier", new BagInfoRequirement(false, Arrays.asList(), false));
    info.put("Bag-Size", new BagInfoRequirement(true, Arrays.asList(), false));
    info.put("Bag-Group-Identifier", new BagInfoRequirement(false, Arrays.asList(), false));
    info.put("Bag-Count", new BagInfoRequirement(true, Arrays.asList(), false));
    info.put("Internal-Sender-Identifier", new BagInfoRequirement(false, Arrays.asList(), false));
    info.put("Internal-Sender-Description", new BagInfoRequirement(false, Arrays.asList(), false));
    info.put("Bagging-Date", new BagInfoRequirement(true, Arrays.asList(), false));
    info.put("Payload-Oxum", new BagInfoRequirement(true, Arrays.asList(), false));

    return info;
  }
}
