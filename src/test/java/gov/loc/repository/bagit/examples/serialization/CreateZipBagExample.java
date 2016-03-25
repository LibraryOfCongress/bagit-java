package gov.loc.repository.bagit.examples.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Example(s) for creating a zipped bag.
 */
public class CreateZipBagExample extends Assert{
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private Path bagRoot;
  private Path zippedBagPath;
  private OutputStream outputStream;
  
  @Before
  public void setup() throws IOException{
    bagRoot = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    zippedBagPath = Paths.get(folder.newFile("bag.zip").toURI());
    outputStream = Files.newOutputStream(zippedBagPath, StandardOpenOption.CREATE);
  }

  @Test
  public void createZipBagWithJavaStandardLibrary(){
    try(ZipOutputStream zip = new ZipOutputStream(outputStream)){
      ZipVistor visitor = new ZipVistor(bagRoot, zip);
      Files.walkFileTree(bagRoot, visitor);
      
      assertTrue(Files.exists(zippedBagPath));
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  class ZipVistor extends SimpleFileVisitor<Path>{
    private Path rootDir;
    private ZipOutputStream zip;
    
    public ZipVistor(Path rootDir, ZipOutputStream zip){
      this.rootDir = rootDir;
      this.zip = zip;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
      try{
        Path relative = rootDir.relativize(file);
        ZipEntry zipEntry = new ZipEntry(relative.toString());
        zip.putNextEntry(zipEntry);
        Files.copy(file, zip);
        zip.closeEntry();
      }
      catch(Exception e){
        e.printStackTrace();
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
