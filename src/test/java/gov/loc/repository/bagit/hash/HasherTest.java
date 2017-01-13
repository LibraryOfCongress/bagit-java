package gov.loc.repository.bagit.hash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;

public class HasherTest extends Assert {

  @Test
  public void testBasicHash() throws IOException, NoSuchAlgorithmException{
    Path path = Paths.get(new File("src/test/resources/bagitFiles/bagit-0.97.txt").toURI());
    String expectedHash = "41b89090f32a9ef33226b48f1b98dddf";
    
    Hasher sut = new MD5Hasher();
    
    sut.hashSingleFile(path);
    String hash = sut.getCalculatedValue();
    assertEquals(expectedHash, hash);
  }
}
