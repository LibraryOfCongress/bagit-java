package gov.loc.repository.bagit.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.loc.repository.bagit.TestUtils;

/**
 * Tests the include or ignore functionality of hidden files while walking the file tree. 
 * Note that this currently only works with linux/unix systems!
 */
public class FileCountAndTotalSizeVistorTest extends Assert {
  
  private Path payloadDir = Paths.get(new File("src/test/resources/hiddenFoldersAndFiles").toURI());
  
  @Before
  public void setup() throws IOException{
    TestUtils.makeFilesHiddenOnWindows(payloadDir);
  }

  @Test
  public void testIgnoreHidden() throws IOException{
    boolean ignoreHiddenFiles = true;
    final FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor(ignoreHiddenFiles);
    
    Files.walkFileTree(payloadDir, vistor);
    
    assertEquals(2, vistor.getCount());
  }
  
  @Test
  public void testDontIgnoreHidden() throws IOException{
    boolean ignoreHiddenFiles = false;
    final FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor(ignoreHiddenFiles);
    
    Files.walkFileTree(payloadDir, vistor);
    assertEquals(5, vistor.getCount());
  }
  
  
}
