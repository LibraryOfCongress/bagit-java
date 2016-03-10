package gov.loc.repository.bagit.tasks;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * A simple task to check if a file exists on the filesystem. This is thread safe, so many can be called at once.
 */
public class CheckIfFileExistsTask implements Runnable {
  private final File file;
  private final StringBuilder messageBuilder;
  private final CountDownLatch latch;
  
  public CheckIfFileExistsTask(File file, StringBuilder messageBuilder, CountDownLatch latch) {
    this.file = file;
    this.messageBuilder = messageBuilder;
    this.latch = latch;
  }

  @Override
  public void run() {
    if(!file.exists()){
      messageBuilder.append("Manifest lists file [").append(file).append("] but it does not exist").append(System.lineSeparator());
    }
    latch.countDown();
  }

}
