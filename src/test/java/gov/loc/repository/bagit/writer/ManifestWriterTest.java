package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class ManifestWriterTest extends PrivateConstructorTest {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(ManifestWriter.class);
  }
  
  @Test
  public void testWriteTagManifests() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(Paths.get("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    File outputDir = folder.newFolder();
    File tagManifest = new File(outputDir, "tagmanifest-md5.txt");
    
    assertFalse(tagManifest.exists());
    ManifestWriter.writeTagManifests(tagManifests, Paths.get(outputDir.toURI()), Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8);
    assertTrue(tagManifest.exists());
  }
  
  @Test
  public void testManifestsDontContainWindowsFilePathSeparator() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(Paths.get("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    File outputDir = folder.newFolder();
    File tagManifest = new File(outputDir, "tagmanifest-md5.txt");
    
    assertFalse(tagManifest.exists());
    ManifestWriter.writeTagManifests(tagManifests, Paths.get(outputDir.toURI()), Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8);
    
    List<String> lines = Files.readAllLines(Paths.get(tagManifest.toURI()));
    for(String line : lines){
      assertFalse("Line [" + line + "] contains \\ which is not allowed by the bagit specification", line.contains("\\"));
    }
  }
}
