package gov.loc.repository.bagit.writer;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Version;

public class BagitFileWriterTest extends PrivateConstructorTest {

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagitFileWriter.class);
  }
  
  @Test
  public void testWriteBagitFile() throws Exception{
    Path rootDir = createDirectory("newFolder");
    Path bagit = rootDir.resolve("bagit.txt");
    
    Assertions.assertFalse(Files.exists(bagit));
    BagitFileWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8, rootDir);
    Assertions.assertTrue(Files.exists(bagit));
    
    //test truncating existing
    long originalModified = Files.getLastModifiedTime(bagit).toMillis();
    long size = Files.size(bagit);
    BagitFileWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8, rootDir);
    Assertions.assertTrue(Files.exists(bagit));
    Assertions.assertTrue(Files.getLastModifiedTime(bagit).toMillis() >= originalModified,
        Files.getLastModifiedTime(bagit) + " should be >= " + originalModified);
    Assertions.assertEquals(size, Files.size(bagit));
  }
}
