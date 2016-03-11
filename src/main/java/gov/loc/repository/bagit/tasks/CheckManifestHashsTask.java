package gov.loc.repository.bagit.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.hash.Hasher;

/**
 * Checks a give file to make sure the given checksum hash matches the computed checksum hash.
 * This is thread safe so you can call many at a time.
 */
public class CheckManifestHashsTask implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(CheckManifestHashsTask.class);
  
  private final Entry<Path, String> entry;
  private final CountDownLatch latch;
  private final List<Exception> exceptions;
  private final String algorithm;
  
  public CheckManifestHashsTask(Entry<Path, String> entry, String algorithm, CountDownLatch latch, List<Exception> exceptions) {
    this.entry = entry;
    this.algorithm = algorithm;
    this.latch = latch;
    this.exceptions = exceptions;
  }

  @Override
  public void run() {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
      checkManifestEntry(entry, messageDigest, algorithm);
    } catch (Exception e) {
      exceptions.add(e);
    }
    latch.countDown();
  }
  
  protected static void checkManifestEntry(Entry<Path, String> entry, MessageDigest messageDigest, String algorithm) throws IOException, CorruptChecksumException{
    if(Files.exists(entry.getKey())){
      logger.debug("Checking file [{}] to see if checksum matches [{}]", entry.getKey(), entry.getValue());
      InputStream inputStream = Files.newInputStream(entry.getKey(), StandardOpenOption.READ);
      String hash = Hasher.hash(inputStream, messageDigest);
      logger.debug("computed hash [{}] for file [{}]", hash, entry.getKey());
      if(!hash.equals(entry.getValue())){
        throw new CorruptChecksumException("File [" + entry.getKey() + "] is suppose to have a " + algorithm + 
            " hash of [" + entry.getValue() + "] but was computed [" + hash+"]");
      }
    }
    //if the file doesn't exist it will be caught by checkAllFilesListedInManifestExist method
  }

}
