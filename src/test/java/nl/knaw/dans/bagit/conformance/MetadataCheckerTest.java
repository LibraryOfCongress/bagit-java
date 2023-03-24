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

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetadataCheckerTest extends PrivateConstructorTest {
  
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MetadataChecker.class);
  }

  @Test
  public void testLinterCheckForPayloadOxum() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();
    MetadataChecker.checkBagMetadata(rootDir, StandardCharsets.UTF_16, warnings, Collections.emptySet());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.PAYLOAD_OXUM_MISSING));
  }
  
  @Test
  public void testLinterIgnorePayloadOxum() throws Exception{
    Set<BagitWarning> warnings = new HashSet<>();
    MetadataChecker.checkBagMetadata(rootDir, StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.PAYLOAD_OXUM_MISSING));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.PAYLOAD_OXUM_MISSING));
  }
}
