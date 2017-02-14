package gov.loc.repository.bagit.hash;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;

public class HasherTest extends PrivateConstructorTest {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(Hasher.class);
  }
  
  @Test
  public void testBasicHash() throws IOException, NoSuchAlgorithmException{
    Path path = Paths.get(new File("src/test/resources/bagitFiles/bagit-0.97.txt").toURI());
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    String expectedHash = "41b89090f32a9ef33226b48f1b98dddf";
    
    String hash = Hasher.hash(path, messageDigest);
    assertEquals(expectedHash, hash);
  }
  
  @Test(expected=IOException.class)
  public void testHashBadInput() throws IOException, NoSuchAlgorithmException{
    Path path = folder.newFolder().toPath();
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    Hasher.updateMessageDigests(path, Arrays.asList(messageDigest));
  }
}
