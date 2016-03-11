package gov.loc.repository.bagit.tasks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * A simple task to check if a file exists on the filesystem. This is thread safe, so many can be called at once.
 */
public class CheckIfFileExistsTask implements Runnable {
  private final Path file;
  private final StringBuilder messageBuilder;
  private final CountDownLatch latch;
  
  public CheckIfFileExistsTask(Path file, StringBuilder messageBuilder, CountDownLatch latch) {
    this.file = file;
    this.messageBuilder = messageBuilder;
    this.latch = latch;
  }

  @Override
  public void run() {
    if(!Files.exists(file)){
      messageBuilder.append("Manifest lists file [").append(file).append("] but it does not exist").append(System.lineSeparator());
    }
    latch.countDown();
  }

}
