package gov.loc.repository.bagit.util;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
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
      String actualEncoded;
      Path testPath;
      	  String fileSeparator = FileSystems.getDefault().getSeparator();
	  StringBuilder sb = new StringBuilder(fileSeparator);
      String basePathString = sb.append("foo").append(fileSeparator).append("bar").
    		  append(fileSeparator).append("ham").toString();
      
    //just carriage return 
      String testPathString = basePathString.concat("\r");
      String expectedEncoded = basePathString.concat("%0D");
      if (PathUtils.isWindows()){
    	  actualEncoded = PathUtils.encodeFilenameString(testPathString);
      }
      else {
    	  testPath = Paths.get(testPathString);
    	  actualEncoded = PathUtils.encodeFilename(testPath);
      }
	  assertEquals(expectedEncoded, actualEncoded);

	  //just new line
	  testPathString = basePathString.concat("\n");
	  expectedEncoded = basePathString.concat("%0A");
	  if (PathUtils.isWindows()){
		  actualEncoded = PathUtils.encodeFilenameString(testPathString);
	  }
	  else {
		  testPath = Paths.get(testPathString);
		  actualEncoded = PathUtils.encodeFilename(testPath);	  
	  }
	  assertEquals(expectedEncoded, actualEncoded);

	  //both carriage return and new line
	  testPathString = basePathString.concat("\r\n");
	  expectedEncoded = basePathString.concat("%0D%0A");
	  if (PathUtils.isWindows()){
		  actualEncoded = PathUtils.encodeFilenameString(testPathString);
	  }
	  else {
		  testPath = Paths.get(testPathString);
		  actualEncoded = PathUtils.encodeFilename(testPath);	  
	  }
	  assertEquals(expectedEncoded, actualEncoded);
  }
}
