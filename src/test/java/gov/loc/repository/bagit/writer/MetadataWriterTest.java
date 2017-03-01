package gov.loc.repository.bagit.writer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Metadata;
import gov.loc.repository.bagit.domain.Version;

public class MetadataWriterTest extends PrivateConstructorTest {

  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(MetadataWriter.class);
  }
  
  @Test
  public void testWriteBagitInfoFile() throws IOException{
    File rootDir = folder.newFolder();
    File bagInfo = new File(rootDir, "bag-info.txt");
    File packageInfo = new File(rootDir, "package-info.txt");
    Metadata metadata = new Metadata();
    metadata.add("key1", "value1");
    metadata.add("key2", "value2");
    metadata.add("key3", "value3");
    
    assertFalse(bagInfo.exists());
    assertFalse(packageInfo.exists());
    
    MetadataWriter.writeBagMetadata(metadata, new Version(0,96), Paths.get(rootDir.toURI()), StandardCharsets.UTF_8);
    assertTrue(bagInfo.exists());
    
    MetadataWriter.writeBagMetadata(metadata, new Version(0,95), Paths.get(rootDir.toURI()), StandardCharsets.UTF_8);
    assertTrue(packageInfo.exists());
  }
}
