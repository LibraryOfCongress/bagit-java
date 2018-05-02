package gov.loc.repository.bagit.writer;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;

public class RelativePathWriterTest extends PrivateConstructorTest {
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(RelativePathWriter.class);
  }
  
  @Test
  public void testRelativePath(){
    Path parent = Paths.get("/foo");
    Path child = parent.resolve("bar/ham");
    String expectedRelativePath = "bar/ham" + System.lineSeparator();
    
    Assertions.assertEquals(expectedRelativePath, RelativePathWriter.formatRelativePathString(parent, child));
  }

  @Test
  public void testUsingBothRelativeAndAbsolutePaths(){
    Path parent = Paths.get("one/two");
    Path child = Paths.get("one/two/three").toAbsolutePath();
    String expectedRelativePath = "three" + System.lineSeparator();

    Assertions.assertEquals(expectedRelativePath, RelativePathWriter.formatRelativePathString(parent, child));
  }
}
