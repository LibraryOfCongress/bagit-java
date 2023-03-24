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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link SimpleFileVisitor} to ensure that the encountered file is in one of the manifests.
 */
public class FileCountAndTotalSizeVistor extends SimpleFileVisitor<Path> {
  private static final Logger logger = LoggerFactory.getLogger(FileCountAndTotalSizeVistor.class);
  private static final ResourceBundle messages = ResourceBundle.getBundle("MessageBundle");
  
  private transient long totalSize;
  private transient long count;

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    if(dir.endsWith(Paths.get(".bagit"))){
      logger.debug(messages.getString("skipping_ignored_directory"), dir);
      return FileVisitResult.SKIP_SUBTREE;
    }
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException{
    count++;
    final long size = Files.size(path);
    logger.debug(messages.getString("file_size_in_bytes"), path, size);
    totalSize += size;
    
    return FileVisitResult.CONTINUE;
  }

  public long getCount() {
    return count;
  }

  public long getTotalSize() {
    return totalSize;
  }
}
