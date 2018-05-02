package gov.loc.repository.bagit.reader;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import gov.loc.repository.bagit.PrivateConstructorTest;
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
    Assertions.assertEquals(1, bag.getPayLoadManifests().size());
    Assertions.assertEquals(1, bag.getTagManifests().size());
  }
  
  @Test
  public void testReadUpDirectoryMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/upAdirectoryReference.txt").toURI());
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8); });
  }
  
  @Test
  public void testReadTildeMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/tildeReference.txt").toURI());
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> { ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8); });
  }
  
  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void testReadFileUrlMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/fileUrl.txt").toURI());
    Assertions.assertThrows(MaliciousPathException.class, 
        () -> {  ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/bar"), StandardCharsets.UTF_8); });
  }
  
  @Test
  public void testReadWindowsSpecialDirMaliciousManifestThrowsException() throws Exception{
    Path manifestFile = Paths.get(getClass().getClassLoader().getResource("maliciousManifestFile/windowsSpecialDirectoryName.txt").toURI());
    Assertions.assertThrows(InvalidBagitFileFormatException.class, 
        () -> { ManifestReader.readChecksumFileMap(manifestFile, Paths.get("/foo"), StandardCharsets.UTF_8); });
  }
}
