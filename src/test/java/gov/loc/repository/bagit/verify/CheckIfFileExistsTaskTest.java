package gov.loc.repository.bagit.verify;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.loc.repository.bagit.TempFolderTest;

public class CheckIfFileExistsTaskTest extends TempFolderTest {

  @Test
  public void testNormalizedFileExists() throws Exception{
    ExecutorService executor = Executors.newCachedThreadPool();
    CountDownLatch latch = new CountDownLatch(1);
    Set<Path> missingFiles = new ConcurrentSkipListSet<>();
    String filename = "Núñez.txt";
    String filenameNFC = Normalizer.normalize(filename, Normalizer.Form.NFC);
    String filenameNFD = Normalizer.normalize(filename, Normalizer.Form.NFD);
    Path NFCPath = folder.resolve(filenameNFC);
    
    createFile(filenameNFD);
    
    CheckIfFileExistsTask sut = new CheckIfFileExistsTask(NFCPath, missingFiles, latch);
    
    executor.execute(sut);
    latch.await();
    executor.shutdown();
    
    Assertions.assertTrue(missingFiles.size() == 0);
  }
}
