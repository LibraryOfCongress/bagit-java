package gov.loc.repository.bagit.writer;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.FetchItem;

public class FetchWriterTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(FetchWriter.class);
  }

  @Test
  public void testWriteFetchFile() throws Exception{
    Path rootPath = createDirectory("fetchTest");
    Path fetch = rootPath.resolve("fetch.txt");
    URL url = new URL("http://localhost:/foo/bar");
    List<FetchItem> itemsToFetch = Arrays.asList(new FetchItem(url, -1l, rootPath.resolve("/data/foo/bar")),
        new FetchItem(url, 100l, rootPath.resolve("/data/foo/bar")));
    
    
    Assertions.assertFalse(Files.exists(fetch));
    FetchWriter.writeFetchFile(itemsToFetch, rootPath, rootPath, StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(fetch));
  }
  
  @Test
  public void testFetchFileIsFormattedCorrectly() throws Exception{
    Path rootPath = createDirectory("fetchFormatted");
    Path fetch = rootPath.resolve("fetch.txt");
    List<FetchItem> itemsToFetch = new ArrayList<>();
    
    itemsToFetch.add(new FetchItem(new URL("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt"), null, rootPath.resolve("data/dir1/test3.txt")));
    itemsToFetch.add(new FetchItem(new URL("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt"), null, rootPath.resolve("data/dir2/dir3/test5.txt")));
    itemsToFetch.add(new FetchItem(new URL("http://localhost:8989/bags/v0_96/holey-bag/data/dir2/test4.txt"), null, rootPath.resolve("data/dir2/test4.txt")));
    itemsToFetch.add(new FetchItem(new URL("http://localhost:8989/bags/v0_96/holey-bag/data/test%201.txt"), null, rootPath.resolve("data/test 1.txt")));
    itemsToFetch.add(new FetchItem(new URL("http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt"), null, rootPath.resolve("data/test2.txt")));
    
    FetchWriter.writeFetchFile(itemsToFetch, rootPath, rootPath, StandardCharsets.UTF_8);
    
    List<String> expectedLines = Arrays.asList("http://localhost:8989/bags/v0_96/holey-bag/data/dir1/test3.txt - data/dir1/test3.txt", 
        "http://localhost:8989/bags/v0_96/holey-bag/data/dir2/dir3/test5.txt - data/dir2/dir3/test5.txt", 
        "http://localhost:8989/bags/v0_96/holey-bag/data/dir2/test4.txt - data/dir2/test4.txt", 
        "http://localhost:8989/bags/v0_96/holey-bag/data/test%201.txt - data/test 1.txt",
        "http://localhost:8989/bags/v0_96/holey-bag/data/test2.txt - data/test2.txt");
    List<String> actualLines = Files.readAllLines(fetch);
    
    Assertions.assertEquals(expectedLines, actualLines);
  }
}
