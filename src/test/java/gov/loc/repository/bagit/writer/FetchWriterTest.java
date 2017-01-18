package gov.loc.repository.bagit.writer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.FetchItem;

public class FetchWriterTest extends PrivateConstructorTest {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(FetchWriter.class);
  }

  @Test
  public void testWriteFetchFile() throws Exception{
    File rootDir = folder.newFolder();
    Path rootPath = rootDir.toPath();
    File fetch = new File(rootDir, "fetch.txt");
    URL url = new URL("http://localhost:/foo/bar");
    List<FetchItem> itemsToFetch = Arrays.asList(new FetchItem(url, -1l, rootPath.resolve("/data/foo/bar")),
        new FetchItem(url, 100l, rootPath.resolve("/data/foo/bar")));
    
    
    assertFalse(fetch.exists());
    FetchWriter.writeFetchFile(itemsToFetch, Paths.get(rootDir.toURI()), rootPath, StandardCharsets.UTF_8);
    assertTrue(fetch.exists());
  }
}
