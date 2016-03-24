package gov.loc.repository.bagit.creator;

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
}
