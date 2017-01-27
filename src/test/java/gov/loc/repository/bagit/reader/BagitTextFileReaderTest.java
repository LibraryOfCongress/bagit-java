package gov.loc.repository.bagit.reader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleImmutableEntry;

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
  
  @Test(expected=UnparsableVersionException.class)
  public void testParseVersionWithBadVersion() throws UnparsableVersionException{
    BagitTextFileReader.parseVersion("someVersionThatIsUnparsable");
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
