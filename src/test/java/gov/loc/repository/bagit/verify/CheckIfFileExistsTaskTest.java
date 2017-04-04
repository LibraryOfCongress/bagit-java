package gov.loc.repository.bagit.verify;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CheckIfFileExistsTaskTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();

  @Test
  public void testNormalizedFileExists() throws Exception{
    ExecutorService executor = Executors.newCachedThreadPool();
    CountDownLatch latch = new CountDownLatch(1);
    Set<Path> missingFiles = new ConcurrentSkipListSet<>();
    String filename = "Núñez.txt";
    String filenameNFC = Normalizer.normalize(filename, Normalizer.Form.NFC);
    String filenameNFD = Normalizer.normalize(filename, Normalizer.Form.NFD);
    
    folder.newFile(filenameNFD); //create the test file on disk
    
    Path NFCPath = folder.getRoot().toPath().resolve(filenameNFC);
    CheckIfFileExistsTask sut = new CheckIfFileExistsTask(NFCPath, missingFiles, latch);
    
    executor.execute(sut);
    latch.await();
    executor.shutdown();
    
    assertTrue(missingFiles.size() == 0);
  }
}
