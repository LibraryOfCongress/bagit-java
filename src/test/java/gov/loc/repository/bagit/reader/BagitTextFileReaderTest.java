package gov.loc.repository.bagit.reader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Version;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.UnparsableVersionException;

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
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testFirstLineMatchesStrict() throws Exception{
    //should fail because it has spaces before the colon
    List<String> lines = Arrays.asList("BagIt-Version    : 1.0", "Tag-File-Character-Encoding: UTF-8");
    BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines);
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testSecondLineMatchesStrict() throws Exception{
    //should fail because it has spaces before the colon
    List<String> lines = Arrays.asList("BagIt-Version: 1.0", "Tag-File-Character-Encoding      : UTF-8");
    BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines);
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testMatchesStrictWithTooManyLines() throws Exception{
    //should fail because it has 3 lines
    List<String> lines = Arrays.asList("BagIt-Version: 1.0", "Tag-File-Character-Encoding: UTF-8", "");
    BagitTextFileReader.throwErrorIfLinesDoNotMatchStrict(lines);
  }
  
  @Test(expected=UnparsableVersionException.class)
  public void testParseVersionWithBadVersion() throws UnparsableVersionException{
    BagitTextFileReader.parseVersion("someVersionThatIsUnparsable");
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
  
  @Test(expected=UnparsableVersionException.class)
  public void testParsePartlyMissingVersion() throws Exception{
    BagitTextFileReader.parseVersion(".97");
  }
  
  @Test
  public void testReadBagitFile()throws Exception{
    Path bagitFile = Paths.get(new File("src/test/resources/bagitFiles/bagit-0.97.txt").toURI());
    SimpleImmutableEntry<Version, Charset> actualBagitInfo = BagitTextFileReader.readBagitTextFile(bagitFile);
    assertEquals(new Version(0, 97), actualBagitInfo.getKey());
    assertEquals(StandardCharsets.UTF_8, actualBagitInfo.getValue());
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testReadBagitFileWithBomShouldThrowException()throws Exception{
    Path bagitFile = Paths.get(new File("src/test/resources/bagitFiles/bagit-with-bom.txt").toURI());
    BagitTextFileReader.readBagitTextFile(bagitFile);
  }
}
