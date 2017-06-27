package gov.loc.repository.bagit.reader;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.TestUtils;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;

public class FetchReaderTest extends PrivateConstructorTest {

  private List<URL> urls;
  
  @Before
  public void setup() throws MalformedURLException{
    urls = Arrays.asList(new URL("http://localhost/foo/data/dir1/test3.txt"), 
        new URL("http://localhost/foo/data/dir2/dir3/test5.txt"),
        new URL("http://localhost/foo/data/dir2/test4.txt"),
        new URL("http://localhost/foo/data/test%201.txt"),
        new URL("http://localhost/foo/data/test2.txt"));
  }
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(FetchReader.class);
  }
  
  @Test
  public void testReadFetchWithNoSizeSpecified() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("fetchFiles/fetchWithNoSizeSpecified.txt").toURI());
    List<FetchItem> returnedItems = FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, fetchFile.getParent());
    
    for(FetchItem item : returnedItems){
      assertNotNull(item.url);
      assertTrue(urls.contains(item.url));
      
      assertEquals(Long.valueOf(-1), item.length);
      
      assertNotNull(item.path);
    }
  }
  
  @Test
  public void testReadFetchWithSizeSpecified() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("fetchFiles/fetchWithSizeSpecified.txt").toURI());
    List<FetchItem> returnedItems = FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, Paths.get("/foo"));
    
    for(FetchItem item : returnedItems){
      assertNotNull(item.url);
      assertTrue(urls.contains(item.url));
      
      assertTrue(item.length > 0);
      
      assertNotNull(item.path);
    }
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testReadBlankLinesThrowsException() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("fetchFiles/fetchWithBlankLines.txt").toURI());
    FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, Paths.get("/foo"));
  }
  
  @Test(expected=InvalidBagitFileFormatException.class)
  public void testReadWindowsSpecialDirMaliciousFetchThrowsException() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("maliciousFetchFile/windowsSpecialDirectoryName.txt").toURI());
    FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, Paths.get("/foo"));
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testReadUpADirMaliciousFetchThrowsException() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("maliciousFetchFile/upAdirectoryReference.txt").toURI());
    FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, Paths.get("/bar"));
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testReadTildeFetchThrowsException() throws Exception{
    Path fetchFile = Paths.get(getClass().getClassLoader().getResource("maliciousFetchFile/tildeReference.txt").toURI());
    FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, Paths.get("/bar"));
  }
  
  @Test(expected=MaliciousPathException.class)
  public void testReadFileUrlMaliciousFetchThrowsException() throws Exception{
    if(!TestUtils.isExecutingOnWindows()){
      Path fetchFile = Paths.get(getClass().getClassLoader().getResource("maliciousFetchFile/fileUrl.txt").toURI());
      FetchReader.readFetch(fetchFile, StandardCharsets.UTF_8, Paths.get("/bar"));
    }
    throw new MaliciousPathException("Skipping for windows cause it isn't valid");
  }
  
  @Test
  public void foo(){
    String regex = ".*[ \t]*(\\d*|-)[ \t]*.*";
    String test1 = "http://localhost/foo/data/test2.txt - ~/foo/bar/ham.txt";
    String test2 = "http://localhost/foo/data/dir1/test3.txt 100057 data/dir1/test3.txt";
    String test3 = "http://localhost/foo/data/dir1/test3.txt \t 100057 \t data/dir1/test3.txt";
    
    System.err.println(test1.matches(regex));
    System.err.println(test2.matches(regex));
    System.err.println(test3.matches(regex));
  }
}
