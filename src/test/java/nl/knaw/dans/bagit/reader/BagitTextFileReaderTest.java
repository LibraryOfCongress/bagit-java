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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;

import nl.knaw.dans.bagit.exceptions.InvalidBagitFileFormatException;
import nl.knaw.dans.bagit.exceptions.UnparsableVersionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.knaw.dans.bagit.PrivateConstructorTest;
import nl.knaw.dans.bagit.domain.Version;

public class BagitTextFileReaderTest extends PrivateConstructorTest {

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagitTextFileReader.class);
  }
  
  @Test
  public void testLinesMatchesStrict() throws Exception{
    List<String> lines = Arrays.asList("BagIt-Version: 1.0", "Tag-File-Character-Encoding: UTF-8");
    BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines);
  }
  
  @Test
  public void testFirstLineMatchesStrict() throws Exception{
    //should fail because it has spaces before the colon
    List<String> lines = Arrays.asList("BagIt-Version    : 1.0", "Tag-File-Character-Encoding: UTF-8");
    Assertions.assertThrows(InvalidBagitFileFormatException.class,
        () -> { BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines); });
  }
  
  @Test
  public void testSecondLineMatchesStrict() throws Exception{
    //should fail because it has spaces before the colon
    List<String> lines = Arrays.asList("BagIt-Version: 1.0", "Tag-File-Character-Encoding      : UTF-8");
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines); });
  }
  
  @Test
  public void testMatchesStrictWithTooManyLines() throws Exception{
    //should fail because it has 3 lines
    List<String> lines = Arrays.asList("BagIt-Version: 1.0", "Tag-File-Character-Encoding: UTF-8", "");
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines); });
  }
  
  @Test
  public void testParseVersionWithBadVersion() throws UnparsableVersionException {
    Assertions.assertThrows(UnparsableVersionException.class, 
        () -> { BagitTextFileReader.parseVersion("someVersionThatIsUnparsable"); });
  }
  
  @Test
  public void testParseKnownVersions() throws Exception{
    String[] knownVersions = new String[] {"0.93", "0.94", "0.95", "0.96", "0.97", "1.0"};
    for(String knownVersion : knownVersions){
      BagitTextFileReader.parseVersion(knownVersion);
    }
  }
  
  @Test
  public void testParseVersionsWithSpaces() throws Exception{
    BagitTextFileReader.parseVersion("1.0 ");
    BagitTextFileReader.parseVersion(" 1.0");
  }
  
  @Test
  public void testParsePartlyMissingVersion() throws Exception{
    Assertions.assertThrows(UnparsableVersionException.class, 
        () -> { BagitTextFileReader.parseVersion(".97"); });
  }
  
  @Test
  public void testReadBagitFile()throws Exception{
    Path bagitFile = Paths.get(new File("src/test/resources/bagitFiles/bagit-0.97.txt").toURI());
    SimpleImmutableEntry<Version, Charset> actualBagitInfo = BagitTextFileReader.readBagitTextFile(bagitFile);
    Assertions.assertEquals(new Version(0, 97), actualBagitInfo.getKey());
    Assertions.assertEquals(StandardCharsets.UTF_8, actualBagitInfo.getValue());
  }
  
  @Test
  public void testReadBagitFileWithBomShouldThrowException()throws Exception{
    Path bagitFile = Paths.get(new File("src/test/resources/bagitFiles/bagit-with-bom.txt").toURI());
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { BagitTextFileReader.readBagitTextFile(bagitFile); });
  }
}
