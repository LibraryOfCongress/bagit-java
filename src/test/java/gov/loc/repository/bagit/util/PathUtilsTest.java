package gov.loc.repository.bagit.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.TestUtils;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Version;

public class PathUtilsTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(PathUtils.class);
  }

  @Test
  public void testDecode(){
    //just carriage return 
    String testString = "/foo/bar/ham%0D";
    String expectedDecoded = "/foo/bar/ham\r";
    String actualDecoded = PathUtils.decodeFilname(testString);
    Assertions.assertEquals(expectedDecoded, actualDecoded);
    
    //just new line
    testString = "/foo/bar/ham%0A";
    expectedDecoded = "/foo/bar/ham\n";
    actualDecoded = PathUtils.decodeFilname(testString);
    Assertions.assertEquals(expectedDecoded, actualDecoded);
    
    //both carriage return and new line
    testString = "/foo/bar/ham%0D%0A";
    expectedDecoded = "/foo/bar/ham\r\n";
    actualDecoded = PathUtils.decodeFilname(testString);
    Assertions.assertEquals(expectedDecoded, actualDecoded);
  }
  
  @Test
  public void testEncode(){
    if(!TestUtils.isExecutingOnWindows()){
      //just carriage return
      Path testPath = Paths.get("foo/bar/ham\r");
      String expectedEncoded = "foo/bar/ham%0D";
      String actualEncoded = PathUtils.encodeFilename(testPath);
      Assertions.assertEquals(expectedEncoded, actualEncoded);
      
      //just new line
      testPath = Paths.get("foo/bar/ham\n");
      expectedEncoded = "foo/bar/ham%0A";
      actualEncoded = PathUtils.encodeFilename(testPath);
      Assertions.assertEquals(expectedEncoded, actualEncoded);
      
      //both carriage return and new line
      testPath = Paths.get("foo/bar/ham\r\n");
      expectedEncoded = "foo/bar/ham%0D%0A";
      actualEncoded = PathUtils.encodeFilename(testPath);
      Assertions.assertEquals(expectedEncoded, actualEncoded);
    }
  }
  
  @Test
  public void testGetDataDirUsingBag() throws IOException{
    Bag bag = new Bag(new Version(2,0));
    bag.setRootDir(Paths.get("foo"));
    
    Path expectedPath = bag.getRootDir();
    Path actualPath = PathUtils.getDataDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    bag = new Bag(new Version(0, 97));
    bag.setRootDir(Paths.get("foo"));
    
    expectedPath = bag.getRootDir().resolve("data");
    actualPath = PathUtils.getDataDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGetDataDirUsingVersion() throws IOException{
    Path input = Paths.get("foo");
    Path expectedPath = input;
    Path actualPath = PathUtils.getDataDir(new Version(2,0), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    expectedPath = input.resolve("data");
    actualPath = PathUtils.getDataDir(new Version(0, 97), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGetBagitDirUsingBag(){
    Bag bag = new Bag(new Version(2,0));
    bag.setRootDir(Paths.get("foo"));
    
    Path expectedPath = bag.getRootDir().resolve(".bagit");
    Path actualPath = PathUtils.getBagitDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    bag = new Bag(new Version(0, 97));
    bag.setRootDir(Paths.get("foo"));
    
    expectedPath = bag.getRootDir();
    actualPath = PathUtils.getBagitDir(bag);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGetBagitDirUsingVersion() throws IOException{
    Path input = Paths.get("foo");
    Path expectedPath = input.resolve(".bagit");
    Path actualPath = PathUtils.getBagitDir(new Version(2,0), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
    
    expectedPath = input;
    actualPath = PathUtils.getBagitDir(new Version(0, 97), input);
    
    Assertions.assertEquals(expectedPath, actualPath);
  }
  
  @Test
  public void testGeneratePayloadOxum() throws IOException{
    Path testPath = Paths.get("src", "test", "resources", "bags", "v0_97", "bag", "data");
    Assertions.assertEquals("25.5", PathUtils.generatePayloadOxum(testPath));
  }
}
