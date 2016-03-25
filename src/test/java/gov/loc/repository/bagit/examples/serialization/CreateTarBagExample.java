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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;

public class CreateTarBagExample extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  
  private Path bagRoot;
  private Path tarredBagPath;
  private OutputStream outputStream;
  
  @Before
  public void setup() throws IOException{
    bagRoot = Paths.get(getClass().getClassLoader().getResource("bags/v0_97/bag").getFile());
    tarredBagPath = Paths.get(folder.newFile("bag.tar").toURI());
    outputStream = Files.newOutputStream(tarredBagPath, StandardOpenOption.CREATE);
  }
  
  @Test
  public void createTarBagWithJTar(){
    try(TarOutputStream out = new TarOutputStream(outputStream);) {
      TarVistor visitor = new TarVistor(out, bagRoot);
      Files.walkFileTree(bagRoot, visitor);
      assertTrue(Files.exists(tarredBagPath));
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
