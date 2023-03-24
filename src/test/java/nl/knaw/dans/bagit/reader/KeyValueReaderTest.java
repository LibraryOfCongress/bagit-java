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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.exceptions.InvalidBagMetadataException;

public class KeyValueReaderTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(KeyValueReader.class);
  }

  @Test
  public void testReadInproperIndentedBagMetadataFileThrowsException() throws Exception{
    Path baginfo = Paths.get(getClass().getClassLoader().getResource("badBagMetadata/badIndent.txt").toURI());
    Assertions.assertThrows(InvalidBagMetadataException.class, 
        () -> { KeyValueReader.readKeyValuesFromFile(baginfo, ":", StandardCharsets.UTF_8); });
  }
  
  @Test
  public void testReadInproperBagMetadataKeyValueSeparatorThrowsException() throws Exception{
    Path baginfo = Paths.get(getClass().getClassLoader().getResource("badBagMetadata/badKeyValueSeparator.txt").toURI());
    Assertions.assertThrows(InvalidBagMetadataException.class, 
        () -> { KeyValueReader.readKeyValuesFromFile(baginfo, ":", StandardCharsets.UTF_8); });
  }
}
