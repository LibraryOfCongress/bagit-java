package gov.loc.repository.bagit.reader;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;

public class TagFileReaderTest extends Assert {

  @Test
  public void testCreateFileFromManifest() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Path path = TagFileReader.createFileFromManifest(bagRootDir, "data/bar/ham.txt");
    assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test
  public void testCreateFileFromManifestWithAsterisk() throws Exception{
    Path bagRootDir = Paths.get("foo");
    Path path = TagFileReader.createFileFromManifest(bagRootDir, "*data/bar/ham.txt");
    assertEquals(bagRootDir.resolve("data/bar/ham.txt"), path);
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testBackslashThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    TagFileReader.createFileFromManifest(bagRootDir, "data\\bar\\ham.txt");
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testOutsideDataDirReferenceThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    TagFileReader.createFileFromManifest(bagRootDir, "/bar/ham.txt");
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testRelativePathOutsideDataDirThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    TagFileReader.createFileFromManifest(bagRootDir, "../bar/ham.txt");
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testHomeDirReferenceThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    TagFileReader.createFileFromManifest(bagRootDir, "~/bar/ham.txt");
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testBadURIThrowsException() throws Exception{
    Path bagRootDir = Paths.get("foo");
    TagFileReader.createFileFromManifest(bagRootDir, "file://C:/foo^");
  }
}
