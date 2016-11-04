package gov.loc.repository.bagit.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class AddPayloadToBagManifestVistorTest extends Assert {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test
  public void includeDotKeepFilesInManifest() throws Exception{
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    boolean includeHiddenFiles = false;
    Path start = Paths.get(new File("src/test/resources/dotKeepExampleBag").toURI()).resolve("data");
    
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHiddenFiles);
    Files.walkFileTree(start, sut);
    
    assertEquals(1, manifest.getFileToChecksumMap().size());
    assertTrue(manifest.getFileToChecksumMap().containsKey(start.resolve("fooDir/.keep")));
  }
  
  @Test
  public void testSkipDotBagitDir() throws IOException{
    Path dotBagitDirectory = Paths.get(folder.newFolder(".bagit").toURI());
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, true);
    FileVisitResult returned = sut.preVisitDirectory(dotBagitDirectory, null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testSkipHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, false);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testIncludeHiddenDirectory() throws IOException{
    Path hiddenDirectory = createHiddenDirectory();
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, true);
    FileVisitResult returned = sut.preVisitDirectory(hiddenDirectory, null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  @Test
  public void testSkipHiddenFile() throws IOException{
    Path hiddenFile = createHiddenFile();
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, false);
    FileVisitResult returned = sut.visitFile(hiddenFile, null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  private Path createHiddenDirectory() throws IOException{
    Path hiddenDirectory = Paths.get(folder.newFolder(".someHiddenDir").toURI());
    
    if(System.getProperty("os.name").contains("Windows")){
      Files.setAttribute(hiddenDirectory, "dos:hidden", Boolean.TRUE);
    }
    
    return hiddenDirectory;
  }
  
  private Path createHiddenFile() throws IOException{
    Path hiddenDirectory = Paths.get(folder.newFile(".someHiddenFile").toURI());
    
    if(System.getProperty("os.name").contains("Windows")){
      Files.setAttribute(hiddenDirectory, "dos:hidden", Boolean.TRUE);
    }
    
    return hiddenDirectory;
  }
}
