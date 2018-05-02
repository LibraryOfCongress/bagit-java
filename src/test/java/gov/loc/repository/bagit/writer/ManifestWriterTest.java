package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class ManifestWriterTest extends PrivateConstructorTest {

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
    Path outputDir = createDirectory("tagManifests");
    Path tagManifest = outputDir.resolve("tagmanifest-md5.txt");
    
    Assertions.assertFalse(Files.exists(tagManifest));
    ManifestWriter.writeTagManifests(tagManifests, outputDir, Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(tagManifest));
  }
  
  @Test
  public void testManifestsDontContainWindowsFilePathSeparator() throws IOException{
    Set<Manifest> tagManifests = new HashSet<>();
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    manifest.getFileToChecksumMap().put(Paths.get("/foo/bar/ham/data/one/two/buckleMyShoe.txt"), "someHashValue");
    tagManifests.add(manifest);
    Path outputDir = createDirectory("noWindowsPathSeparator");
    Path tagManifest = outputDir.resolve("tagmanifest-md5.txt");
    
    Assertions.assertFalse(Files.exists(tagManifest));
    ManifestWriter.writeTagManifests(tagManifests, outputDir, Paths.get("/foo/bar/ham"), StandardCharsets.UTF_8);
    
    List<String> lines = Files.readAllLines(tagManifest);
    for(String line : lines){
      Assertions.assertFalse(line.contains("\\"), 
          "Line [" + line + "] contains \\ which is not allowed by the bagit specification");
    }
  }
}
