package gov.loc.repository.bagit.creator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;

public class AddPayloadToBagManifestVistorTest extends Assert {

  @Test
  public void includeDotKeepFilesInManifest() throws Exception{
    Manifest manifest = new Manifest(StandardSupportedAlgorithms.MD5);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    boolean includeHiddenFiles = false;
    Path start = Paths.get(getClass().getClassLoader().getResource("dotKeepExampleBag").getFile()).resolve("data");
    
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(manifest, messageDigest, includeHiddenFiles);
    Files.walkFileTree(start, sut);
    
    assertEquals(1, manifest.getFileToChecksumMap().size());
    assertTrue(manifest.getFileToChecksumMap().containsKey(start.resolve("fooDir/.keep")));
  }
  
  @Test
  public void testSkipDotBagitDir() throws IOException{
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, true);
    FileVisitResult returned = sut.preVisitDirectory(Paths.get("/foo/.bagit"), null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testSkipHiddenDirectory() throws IOException{
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, false);
    FileVisitResult returned = sut.preVisitDirectory(Paths.get("/foo/.someHiddenDir"), null);
    assertEquals(FileVisitResult.SKIP_SUBTREE, returned);
  }
  
  @Test
  public void testIncludeHiddenDirectory() throws IOException{
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, true);
    FileVisitResult returned = sut.preVisitDirectory(Paths.get("/foo/.someHiddenDir"), null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
  
  @Test
  public void testSkipHiddenFile() throws IOException{
    AddPayloadToBagManifestVistor sut = new AddPayloadToBagManifestVistor(null, null, false);
    FileVisitResult returned = sut.visitFile(Paths.get("/foo/.someHiddenDir"), null);
    assertEquals(FileVisitResult.CONTINUE, returned);
  }
}
