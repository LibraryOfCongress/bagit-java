package gov.loc.repository.bagit.writer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gov.loc.repository.bagit.PrivateConstructorTest;
import gov.loc.repository.bagit.domain.Version;

public class BagitFileWriterTest extends PrivateConstructorTest {
  
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test
  public void testClassIsWellDefined() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    assertUtilityClassWellDefined(BagitFileWriter.class);
  }
  
  @Test
  public void testWriteBagitFile() throws Exception{
    File rootDir = folder.newFolder();
    Path rootDirPath = Paths.get(rootDir.toURI());
    Path bagit = rootDirPath.resolve("bagit.txt");
    
    assertFalse(Files.exists(bagit));
    BagitFileWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8, rootDirPath);
    assertTrue(Files.exists(bagit));
    
    //test truncating existing
    long originalModified = Files.getLastModifiedTime(bagit).toMillis();
    long size = Files.size(bagit);
    BagitFileWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8, rootDirPath);
    assertTrue(Files.exists(bagit));
    assertTrue(Files.getLastModifiedTime(bagit) + " should be >= " + originalModified, 
        Files.getLastModifiedTime(bagit).toMillis() >= originalModified);
    assertEquals(size, Files.size(bagit));
  }
  
  @Test
  public void testBagitFileWritesOptionalLines() throws Exception{
    File rootDir = folder.newFolder();
    Path rootDirPath = Paths.get(rootDir.toURI());
    Path bagit = rootDirPath.resolve("bagit.txt");
    
    assertFalse(Files.exists(bagit));
    BagitFileWriter.writeBagitFile(new Version(1, 0), StandardCharsets.UTF_8, 5l, 5l, rootDirPath);
    assertTrue(Files.exists(bagit));
    assertEquals(4, Files.readAllLines(bagit).size());
  }
  
  @Test //should not write payload byte and file count lines for version older than 1.0
  public void testBagitFileDoesntWritesOptionalLines() throws Exception{
    File rootDir = folder.newFolder();
    Path rootDirPath = Paths.get(rootDir.toURI());
    Path bagit = rootDirPath.resolve("bagit.txt");
    
    assertFalse(Files.exists(bagit));
    BagitFileWriter.writeBagitFile(new Version(0, 97), StandardCharsets.UTF_8, 5l, 5l, rootDirPath);
    assertTrue(Files.exists(bagit));
    assertEquals(2, Files.readAllLines(bagit).size());
  }
  
}
