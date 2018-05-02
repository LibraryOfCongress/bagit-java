package gov.loc.repository.bagit.writer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Metadata;
import gov.loc.repository.bagit.domain.Version;

public class MetadataWriterTest extends PrivateConstructorTest {
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MetadataWriter.class);
  }
  
  @Test
  public void testWriteBagitInfoFile() throws IOException{
    Path rootDir = createDirectory("writeBagitInfo");
    Path bagInfo = rootDir.resolve("bag-info.txt");
    Path packageInfo = rootDir.resolve("package-info.txt");
    Metadata metadata = new Metadata();
    metadata.add("key1", "value1");
    metadata.add("key2", "value2");
    metadata.add("key3", "value3");
    
    Assertions.assertFalse(Files.exists(bagInfo));
    Assertions.assertFalse(Files.exists(packageInfo));
    
    MetadataWriter.writeBagMetadata(metadata, new Version(0,96), rootDir, StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(bagInfo));
    
    MetadataWriter.writeBagMetadata(metadata, new Version(0,95), rootDir, StandardCharsets.UTF_8);
    Assertions.assertTrue(Files.exists(packageInfo));
  }
}
