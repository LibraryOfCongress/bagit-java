package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.TestUtils;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class AddPayloadToBagManifestVistorTest extends Assert {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test
  public void includeDotKeepFilesInManifest() throws Exception{
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    Map<Manifest, MessageDigest> map = new HashMap<>();
    map.put(manifest, messageDigest);
    boolean includeHiddenFiles = false;
    Path start = Paths.get(new File("src/test/resources/dotKeepExampleBag").toURI()).resolve("data");
    
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(map, includeHiddenFiles);
    Files.walkFileTree(start, sut);
    
    assertEquals(1, manifest.getFileToChecksumMap().size());
    assertTrue(manifest.getFileToChecksumMap().containsKey(start.resolve("fooDir/.keep")));
  }
  
  @Test
  public void testSkipDotBagitDir() throws IOException{
    Path dotBagitDirectory = Paths.get(folder.newFolder(".bagit").toURI());
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, true);
    FileVisitResult returned = sut.preVisitDirectory(dotBagitDirectory, null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testSkipHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, false);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testIncludeHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, true);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  @Test
  public void testSkipHiddenFile() throws IOException{
    Path hiddenFile = createHiddenFile();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(null, false);
    FileVisitResult returned = sut.visitFile(hiddenFile, null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  private Path createHiddenDirectory() throws IOException{
    Path hiddenDirectory = Paths.get(folder.newFolder(".someHiddenDir").toURI());
    
    if(TestUtils.isExecutingOnWindows()){
      Files.setAttribute(hiddenDirectory, "dos:hidden", Boolean.TRUE);
    }
    
    return hiddenDirectory;
  }
  
  private Path createHiddenFile() throws IOException{
    Path hiddenDirectory = Paths.get(folder.newFile(".someHiddenFile").toURI());
    
    if(TestUtils.isExecutingOnWindows()){
      Files.setAttribute(hiddenDirectory, "dos:hidden", Boolean.TRUE);
    }
    
    return hiddenDirectory;
  }
}
