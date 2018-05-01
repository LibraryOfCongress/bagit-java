package gov.loc.repository.bagit.examples.serialization;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;

import gov.loc.repository.bagit.TempFolderTest;

public class CreateTarBagExample extends TempFolderTest {
  
  private Path bagRoot;
  private Path tarredBagPath;
  private OutputStream outputStream;
  
  @BeforeEach
  public void setup() throws IOException{
    bagRoot = Paths.get(new File("src/test/resources/bags/v0_97/bag").toURI());
    tarredBagPath = createFile("bag.tar");
    outputStream = Files.newOutputStream(tarredBagPath, StandardOpenOption.CREATE);
  }
  
  /**
   * <b> THIS IS JUST AN EXAMPLE. DO NOT USE IN PRODUCTION!</b>
   */
  @Test
  public void createTarBagWithJTar(){
    try(TarOutputStream out = new TarOutputStream(outputStream);) {
      TarVistor visitor = new TarVistor(out, bagRoot);
      Files.walkFileTree(bagRoot, visitor);
      Assertions.assertTrue(Files.exists(tarredBagPath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  class TarVistor extends SimpleFileVisitor<Path>{
    private TarOutputStream tar;
    private Path bagRoot;
    
    public TarVistor(TarOutputStream tar, Path bagRoot){
      this.tar = tar;
      this.bagRoot = bagRoot;
    }
    
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException
    {
      try{
        Path relative = bagRoot.relativize(path);
        TarEntry tarEntry = new TarEntry(path.toFile(), relative.toString());
        tar.putNextEntry(tarEntry);
        Files.copy(path, tar);
      }
      catch(Exception e){
        e.printStackTrace();
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
