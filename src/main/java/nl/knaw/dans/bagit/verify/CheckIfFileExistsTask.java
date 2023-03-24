/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.verify;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple task to check if a file exists on the filesystem. This is thread safe, so many can be called at once.
 */
@SuppressWarnings(value = {"PMD.DoNotUseThreads"})
public class CheckIfFileExistsTask implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(CheckIfFileExistsTask.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  private transient final Path file;
  private transient final Set<Path> missingFiles;
  private transient final CountDownLatch latch;
  
  public CheckIfFileExistsTask(final Path file, final Set<Path> missingFiles, final CountDownLatch latch) {
    this.file = file;
    this.latch = latch;
    this.missingFiles = missingFiles;
  }

  @Override
  public void run() {
    final boolean fileExists = Files.exists(file);
    
    if(!fileExists){
      if(existsNormalized()){
        logger.warn(messages.getString("different_normalization_on_filesystem_warning"), file);
      }
      else{
        missingFiles.add(file);
      }
    }
    
    latch.countDown();
  }
  
  /**
   * if a file is parially normalized or of a different normalization then the manifest specifies it will fail the existence test.
   * This method checks for that by normalizing what is on disk with the normalized filename and see if they match.
   * 
   * @return true if the normalized filename matches one on disk in the specified folder
   */
  private boolean existsNormalized(){
    final String normalizedFile = Normalizer.normalize(file.toString(), Normalizer.Form.NFD);
    final Path parent = file.getParent();
    if(parent != null){
      try(final DirectoryStream<Path> files = Files.newDirectoryStream(parent)){
        for(final Path fileToCheck : files){
          final String normalizedFileToCheck = Normalizer.normalize(fileToCheck.toString(), Normalizer.Form.NFD);
          if(normalizedFile.equals(normalizedFileToCheck)){
            return true;
          }
        }
      }
      catch(IOException e){
        logger.error(messages.getString("error_reading_normalized_file"), parent, normalizedFile, e);
      }
    }
    
    return false;
  }
}
