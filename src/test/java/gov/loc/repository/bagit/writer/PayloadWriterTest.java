package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Manifest;

public class PayloadWriterTest extends PrivateConstructorTest {

  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(PayloadWriter.class);
  }
  
  @Test
  public void testWritePayloadFiles() throws IOException, URISyntaxException{
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Path testFile = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").toURI());
    Manifest manifest = new Manifest("md5");
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    File outputDir = folder.newFolder();
    File copiedFile = new File(outputDir, "data/dir1/test3.txt");
    
    assertFalse(copiedFile.exists() || copiedFile.getParentFile().exists());
    PayloadWriter.writePayloadFiles(payloadManifests, Paths.get(outputDir.toURI()), rootDir);
    assertTrue(copiedFile.exists() && copiedFile.getParentFile().exists());
  }
}
