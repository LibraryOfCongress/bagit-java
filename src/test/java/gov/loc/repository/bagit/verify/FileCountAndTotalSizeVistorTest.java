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
 * Tests the ignore of hidden files while walking the file tree. 
 */
public class FileCountAndTotalSizeVistorTest extends Assert {
  
  private Path payloadDir = Paths.get(new File("src/test/resources/hiddenFoldersAndFiles").toURI());
  
  @Before
  public void setup() throws IOException{
    TestUtils.makeFilesHiddenOnWindows(payloadDir);
  }
  
  @Test
  public void testGeneratePayloadOxum() throws IOException{
    final FileCountAndTotalSizeVistor vistor = new FileCountAndTotalSizeVistor();
    Files.walkFileTree(payloadDir, vistor);
    assertEquals(5, vistor.getCount());
    assertEquals(101, vistor.getTotalSize());
  }
}
