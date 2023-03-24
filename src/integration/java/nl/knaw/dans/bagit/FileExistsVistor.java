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
package nl.knaw.dans.bagit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.Assertions;

public class FileExistsVistor extends SimpleFileVisitor<Path>{
  private transient final Path originalBag;
  private transient final Path newBag;
  
  public FileExistsVistor(final Path originalBag, final Path newBag){
    this.originalBag = originalBag;
    this.newBag = newBag;
  }

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    Path relative = originalBag.relativize(dir);
    Assertions.assertTrue(Files.exists(newBag.resolve(relative)));
    
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)throws IOException{
    final Path relative = originalBag.relativize(path);
    Assertions.assertTrue(Files.exists(newBag.resolve(relative)));
    
    return FileVisitResult.CONTINUE;
  }
  
}
