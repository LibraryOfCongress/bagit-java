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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncodingCheckerTest {

  @Test
  public void testLinterCheckTagFilesEncoding(){
    Set<BagitWarning> warnings = new HashSet<>();
    EncodingChecker.checkEncoding(StandardCharsets.UTF_16, warnings, Collections.emptySet());
    
    Assertions.assertTrue(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
  
  @Test
  public void testLinterIgnoreTagFilesEncoding(){
    Set<BagitWarning> warnings = new HashSet<>();
    EncodingChecker.checkEncoding(StandardCharsets.UTF_16, warnings, Arrays.asList(BagitWarning.TAG_FILES_ENCODING));
    
    Assertions.assertFalse(warnings.contains(BagitWarning.TAG_FILES_ENCODING));
  }
}
