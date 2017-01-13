package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.TestUtils;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.Hasher;
import gov.loc.repository.bagit.hash.MD5Hasher;

public class AddPayloadToBagManifestVistorTest extends Assert {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private Map<String, Hasher> bagitNameToHasherMap;
  
  @Before
  public void setup() throws NoSuchAlgorithmException{
    bagitNameToHasherMap = new HashMap<>();
    bagitNameToHasherMap.put("md5", new MD5Hasher());
  }

  @Test
  public void includeDotKeepFilesInManifest() throws Exception{
    boolean includeHiddenFiles = false;
    Path start = Paths.get(new File("src/test/resources/dotKeepExampleBag").toURI()).resolve("data");
    
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(bagitNameToHasherMap, includeHiddenFiles);
    Files.walkFileTree(start, sut);
    
    assertEquals(1, sut.getManifests().size());
    Manifest manifest = (Manifest) sut.getManifests().toArray()[0];
    assertTrue(manifest.getFileToChecksumMap().containsKey(start.resolve("fooDir/.keep")));
  }
  
  @Test
  public void testSkipDotBagitDir() throws IOException{
    Path dotBagitDirectory = Paths.get(folder.newFolder(".bagit").toURI());
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(bagitNameToHasherMap, true);
    FileVisitResult returned = sut.preVisitDirectory(dotBagitDirectory, null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testSkipHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(bagitNameToHasherMap, false);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testIncludeHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(bagitNameToHasherMap, true);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  @Test
  public void testSkipHiddenFile() throws IOException{
    Path hiddenFile = createHiddenFile();
    CreatePayloadManifestsVistor sut = new CreatePayloadManifestsVistor(bagitNameToHasherMap, false);
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
