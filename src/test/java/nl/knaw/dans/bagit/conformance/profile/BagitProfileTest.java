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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BagitProfileTest extends AbstractBagitProfileTest {

    @Test
    public void testToString() throws Exception {
        String expectedOutput = "BagitProfile [bagitProfileIdentifier=http://canadiana.org/standards/bagit/tdr_ingest.json, "
            + "sourceOrganization=Candiana.org, "
            + "externalDescription=BagIt profile for ingesting content into the C.O. TDR loading dock., "
            + "contactName=William Wueppelmann, "
            + "contactEmail=tdr@canadiana.com, "
            + "contactPhone=+1 613 907 7040, "
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
        Assertions.assertEquals(expectedOutput, profile.toString());
    }

    @Test
    public void testEquals() {
        BagitProfile profile = createExpectedProfile();

        assertNotEquals(null, profile);

        BagitProfile differentBagitProfileIdentifier = createExpectedProfile();
        differentBagitProfileIdentifier.setBagitProfileIdentifier("foo");
        assertNotEquals(profile, differentBagitProfileIdentifier);

        BagitProfile differentSourceOrganization = createExpectedProfile();
        differentSourceOrganization.setSourceOrganization("foo");
        assertNotEquals(profile, differentSourceOrganization);

        BagitProfile differentExternalDescription = createExpectedProfile();
        differentExternalDescription.setExternalDescription("foo");
        assertNotEquals(profile, differentExternalDescription);

        BagitProfile differentContactName = createExpectedProfile();
        differentContactName.setContactName("foo");
        assertNotEquals(profile, differentContactName);

        BagitProfile differentContactEmail = createExpectedProfile();
        differentContactEmail.setContactEmail("foo");
        assertNotEquals(profile, differentContactEmail);

        BagitProfile differentContactPhone = createExpectedProfile();
        differentContactPhone.setContactPhone("foo");
        assertNotEquals(profile, differentContactPhone);

        BagitProfile differentVersion = createExpectedProfile();
        differentVersion.setVersion("foo");
        assertNotEquals(profile, differentVersion);

        BagitProfile differentBagInfoRequirements = createExpectedProfile();
        differentBagInfoRequirements.setBagInfoRequirements(new HashMap<>());
        assertNotEquals(profile, differentBagInfoRequirements);

        BagitProfile differentManifestTypesRequired = createExpectedProfile();
        differentManifestTypesRequired.setManifestTypesRequired(List.of("foo"));
        assertNotEquals(profile, differentManifestTypesRequired);

        BagitProfile differentFetchFileAllowed = createExpectedProfile();
        differentFetchFileAllowed.setFetchFileAllowed(true);
        assertNotEquals(profile, differentFetchFileAllowed);

        BagitProfile differentSerialization = createExpectedProfile();
        differentSerialization.setSerialization(Serialization.required);
        assertNotEquals(profile, differentSerialization);

        BagitProfile differentAcceptableMIMESerializationTypes = createExpectedProfile();
        differentAcceptableMIMESerializationTypes.setAcceptableMIMESerializationTypes(List.of("foo"));
        assertNotEquals(profile, differentAcceptableMIMESerializationTypes);

        BagitProfile differentAcceptableBagitVersions = createExpectedProfile();
        differentAcceptableBagitVersions.setAcceptableBagitVersions(List.of("foo"));
        assertNotEquals(profile, differentAcceptableBagitVersions);

        BagitProfile differentTagManifestTypesRequired = createExpectedProfile();
        differentTagManifestTypesRequired.setTagManifestTypesRequired(List.of("foo"));
        assertNotEquals(profile, differentTagManifestTypesRequired);

        BagitProfile differentTagFilesRequired = createExpectedProfile();
        differentTagFilesRequired.setTagFilesRequired(List.of("foo"));
        assertNotEquals(profile, differentTagFilesRequired);
    }
}
