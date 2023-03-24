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
package nl.knaw.dans.bagit.reader;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;

public class MetadataReaderTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MetadataReader.class);
  }

  @Test
  public void testReadBagMetadata() throws Exception{
    List<SimpleImmutableEntry<String, String>> expectedValues = new ArrayList<>();
    expectedValues.add(new SimpleImmutableEntry<>("Source-Organization", "Spengler University"));
    expectedValues.add(new SimpleImmutableEntry<>("Organization-Address", "1400 Elm St., Cupertino, California, 95014"));
    expectedValues.add(new SimpleImmutableEntry<>("Contact-Name", "Edna Janssen"));
    expectedValues.add(new SimpleImmutableEntry<>("Contact-Phone", "+1 408-555-1212"));
    expectedValues.add(new SimpleImmutableEntry<>("Contact-Email", "ej@spengler.edu"));
    expectedValues.add(new SimpleImmutableEntry<>("External-Description", "Uncompressed greyscale TIFF images from the" + System.lineSeparator() + 
        "         Yoshimuri papers collection."));
    expectedValues.add(new SimpleImmutableEntry<>("Bagging-Date", "2008-01-15"));
    expectedValues.add(new SimpleImmutableEntry<>("External-Identifier", "spengler_yoshimuri_001"));
    expectedValues.add(new SimpleImmutableEntry<>("Bag-Size", "260 GB"));
    expectedValues.add(new SimpleImmutableEntry<>("Bag-Group-Identifier", "spengler_yoshimuri"));
    expectedValues.add(new SimpleImmutableEntry<>("Bag-Count", "1 of 15"));
    expectedValues.add(new SimpleImmutableEntry<>("Internal-Sender-Identifier", "/storage/images/yoshimuri"));
    expectedValues.add(new SimpleImmutableEntry<>("Internal-Sender-Description", "Uncompressed greyscale TIFFs created from" + System.lineSeparator() + 
        "         microfilm."));
    expectedValues.add(new SimpleImmutableEntry<>("Bag-Count", "1 of 15")); //test duplicate
    
    Path bagInfoFile = Paths.get(getClass().getClassLoader().getResource("baginfoFiles").toURI());
    List<SimpleImmutableEntry<String, String>> actualMetadata = MetadataReader.readBagMetadata(bagInfoFile, StandardCharsets.UTF_8);
    
    Assertions.assertEquals(expectedValues, actualMetadata);
  }
}
