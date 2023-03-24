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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.reader.BagReader;

public class BagLinterTest extends PrivateConstructorTest {
  
  private final Path rootDir = Paths.get("src","test","resources","linterTestBag");
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagLinter.class);
  }
  
  @Test
  public void testConformantBag() throws Exception{
	  Path goodBag = Paths.get("src", "test", "resources", "bags", "v1_0", "bag");
	  Set<BagitWarning> warnings = BagLinter.lintBag(goodBag);
	  Assertions.assertTrue(warnings.size() == 0);
  }
  
  @Test
  public void testLintBag() throws Exception{
    Set<BagitWarning> expectedWarnings = new HashSet<>();
    expectedWarnings.addAll(Arrays.asList(BagitWarning.values()));
    expectedWarnings.remove(BagitWarning.MANIFEST_SETS_DIFFER); //only applies to version 1.0 but need older version for other warnings, so we test this separately
    Set<BagitWarning> warnings = BagLinter.lintBag(rootDir);

    if(FileSystems.getDefault().getClass().getName() == "sun.nio.fs.MacOSXFileSystem"){ //don't test normalization on mac
      expectedWarnings.remove(BagitWarning.DIFFERENT_NORMALIZATION);
      warnings.remove(BagitWarning.DIFFERENT_NORMALIZATION);
    }
    
    Set<BagitWarning> diff = new HashSet<>(expectedWarnings);
    diff.removeAll(warnings);
    
    Assertions.assertEquals(expectedWarnings, warnings, "Warnings missing: " + diff.toString() + "\n");
  }
  
  @Test
  public void testCheckAgainstProfile() throws Exception{
    Path profileJson = new File("src/test/resources/bagitProfiles/exampleProfile.json").toPath();
    Path bagRootPath = new File("src/test/resources/bagitProfileTestBags/profileConformantBag").toPath();
    BagReader reader = new BagReader();
    Bag bag = reader.read(bagRootPath);
    
    try(InputStream inputStream = Files.newInputStream(profileJson, StandardOpenOption.READ)){
      BagLinter.checkAgainstProfile(inputStream, bag);
    }
  }
  
  @Test
  public void testIgnoreCheckForExtraLines() throws Exception{
    Set<BagitWarning> warnings = BagLinter.lintBag(rootDir, Arrays.asList(BagitWarning.EXTRA_LINES_IN_BAGIT_FILES));
    Assertions.assertFalse(warnings.contains(BagitWarning.EXTRA_LINES_IN_BAGIT_FILES));
  }
}