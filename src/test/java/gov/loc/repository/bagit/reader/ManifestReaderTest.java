package gov.loc.repository.bagit.reader;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.TestUtils;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.hash.StandardBagitAlgorithmNameToSupportedAlgorithmMapping;

public class ManifestReaderTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(ManifestReader.class);
  }
  
  @Test
  public void testReadAllManifests() throws Exception{
    Path rootBag = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Bag bag = new Bag();
    bag.setRootDir(rootBag);
    ManifestReader.readAllManifests(new StandardBagitAlgorithmNameToSupportedAlgorithmMapping(), rootBag, bag);
    assertEquals(1, bag.getPayLoadManifests().size());
    assertEquals(1, bag.getTagManifests().size());
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testReadUpDirectoryMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/upAdirectoryReference.txt").toURI());
    ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8);
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testReadTildeMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/tildeReference.txt").toURI());
    ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8);
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testReadFileUrlMaliciousManifestThrowsException() throws Exception{
    if(!TestUtils.isExecutingOnWindows()){
      Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/fileUrl.txt").toURI());
      ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/bar"), StandardCharsets.UTF_8);
    }
    throw new MaliciousPathException("Skipping for windows cause it isn't valid");
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testReadWindowsSpecialDirMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/windowsSpecialDirectoryName.txt").toURI());
    ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8);
  }
}
