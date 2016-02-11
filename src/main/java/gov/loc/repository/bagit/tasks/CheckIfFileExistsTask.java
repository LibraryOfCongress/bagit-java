package gov.loc.repository.bagit.tasks;

import java.io.File;
import java.util.concurrent.CountDownLatch;

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
