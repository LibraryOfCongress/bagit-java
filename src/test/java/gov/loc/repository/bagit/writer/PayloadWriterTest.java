package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.FetchItem;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class PayloadWriterTest extends PrivateConstructorTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testClassIsWellDefined()
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    assertUtilityClassWellDefined(PayloadWriter.class);
  }

  @Test
  public void testWritePayloadFiles() throws IOException, URISyntaxException {
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Path testFile = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").toURI());
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    File outputDir = folder.newFolder();
    File copiedFile = new File(outputDir, "data/dir1/test3.txt");

    assertFalse(copiedFile.exists() || copiedFile.getParentFile().exists());
    PayloadWriter.writePayloadFiles(payloadManifests, new ArrayList<>(), Paths.get(outputDir.toURI()), rootDir);
    assertTrue(copiedFile.exists() && copiedFile.getParentFile().exists());
  }

  @Test
  public void testWritePayloadFilesMinusFetchFiles() throws IOException, URISyntaxException {
    Path rootDir = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").toURI());
    Path testFile = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag/data/dir1/test3.txt").toURI());
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(testFile, "someHashValue");
    Set<Manifest> payloadManifests = new HashSet<>();
    payloadManifests.add(manifest);
    File outputDir = folder.newFolder();
    File copiedFile = new File(outputDir, "data/dir1/test3.txt");

    assertFalse(copiedFile.exists() || copiedFile.getParentFile().exists());
    PayloadWriter.writePayloadFiles(payloadManifests,
        Arrays.asList(new FetchItem(null, null, Paths.get("data/dir1/test3.txt"))), Paths.get(outputDir.toURI()),
        rootDir.resolve("data"));
    assertFalse(copiedFile.exists() && copiedFile.getParentFile().exists());
  }
}
