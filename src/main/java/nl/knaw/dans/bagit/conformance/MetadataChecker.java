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
package nl.knaw.dans.bagit.conformance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;
import nl.knaw.dans.bagit.reader.MetadataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part of the BagIt conformance suite. 
 * This checker checks the bag metadata (bag-info.txt) for various problems.
 */
public final class MetadataChecker {
  private static final Logger logger = LoggerFactory.getLogger(MetadataChecker.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  private MetadataChecker(){
    //intentionally left empty
  }
  
  public static void checkBagMetadata(final Path bagitDir, final Charset encoding, final Set<BagitWarning> warnings, 
      final Collection<BagitWarning> warningsToIgnore) throws IOException, InvalidBagMetadataException {
    checkForPayloadOxumMetadata(bagitDir, encoding, warnings, warningsToIgnore);
  }
  
  /*
   * Check that the metadata contains the Payload-Oxum key-value pair
   */
  private static void checkForPayloadOxumMetadata(final Path bagitDir, final Charset encoding, final Set<BagitWarning> warnings, 
      final Collection<BagitWarning> warningsToIgnore) throws IOException, InvalidBagMetadataException{
    if(!warningsToIgnore.contains(BagitWarning.PAYLOAD_OXUM_MISSING)){
      final List<SimpleImmutableEntry<String, String>> metadata = MetadataReader.readBagMetadata(bagitDir, encoding);
      boolean containsPayloadOxum = false;
      
      for(final SimpleImmutableEntry<String, String> pair : metadata){
        if("Payload-Oxum".equals(pair.getKey())){
          containsPayloadOxum = true;
        }
      }
      
      if(!containsPayloadOxum){
        logger.warn(messages.getString("missing_payload_oxum_warning"));
        warnings.add(BagitWarning.PAYLOAD_OXUM_MISSING);
      }
    }
  }
}
