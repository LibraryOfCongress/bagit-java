package gov.loc.repository.bagit.util;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;

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
    assertEquals(expectedDecoded, actualDecoded);
    
    //just new line
    testString = "/foo/bar/ham%0A";
    expectedDecoded = "/foo/bar/ham\n";
    actualDecoded = PathUtils.decodeFilname(testString);
    assertEquals(expectedDecoded, actualDecoded);
    
    //both carriage return and new line
    testString = "/foo/bar/ham%0D%0A";
    expectedDecoded = "/foo/bar/ham\r\n";
    actualDecoded = PathUtils.decodeFilname(testString);
    assertEquals(expectedDecoded, actualDecoded);
  }
  
  @Test
  public void testEncode(){
	if (System.getProperty("os.name").contains("Windows")){
	    //just carriage return
	    String testPath = "\\foo\\bar\\ham\r";
	    String expectedEncoded = "\\foo\\bar\\ham%0D";
	    String actualEncoded = PathUtils.encodeFilename(testPath);
	    assertEquals(expectedEncoded, actualEncoded);
		    
	    //just new line
	    testPath = "\\foo\\bar\\ham\n";
	    expectedEncoded = "\\foo\\bar\\ham%0A";
	    actualEncoded = PathUtils.encodeFilename(testPath);
	    assertEquals(expectedEncoded, actualEncoded);
	    
	    //both carriage return and new line
	    testPath = "\\foo\\bar\\ham\r\n";
	    expectedEncoded = "\\foo\\bar\\ham%0D%0A";
	    actualEncoded = PathUtils.encodeFilename(testPath);
	    assertEquals(expectedEncoded, actualEncoded);		
	}
	else {
	    //just carriage return
	    Path testPath = Paths.get("/foo/bar/ham\r");
	    String expectedEncoded = "/foo/bar/ham%0D";
	    String actualEncoded = PathUtils.encodeFilename(testPath);
	    assertEquals(expectedEncoded, actualEncoded);
		    
	    //just new line
	    testPath = Paths.get("/foo/bar/ham\n");
	    expectedEncoded = "/foo/bar/ham%0A";
	    actualEncoded = PathUtils.encodeFilename(testPath);
	    assertEquals(expectedEncoded, actualEncoded);
	    
	    //both carriage return and new line
	    testPath = Paths.get("/foo/bar/ham\r\n");
	    expectedEncoded = "/foo/bar/ham%0D%0A";
	    actualEncoded = PathUtils.encodeFilename(testPath);
	    assertEquals(expectedEncoded, actualEncoded);
	}
  }
}
