package gov.loc.repository.bagit.hash;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;

public class HasherTest extends PrivateConstructorTest {

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(Hasher.class);
  }
  
  @Test
  public void testBasicHash() throws IOException, NoSuchAlgorithmException{
    Path path = Paths.get(getClass().getClassLoader().getResource("bagitFiles/bagit-0.97.txt").getFile());
    InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    String expectedHash = "41b89090f32a9ef33226b48f1b98dddf";
    
    String hash = Hasher.hash(inputStream, messageDigest);
    assertEquals(expectedHash, hash);
  }
}
