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

public class TestUtils {
  public static boolean isExecutingOnWindows(){
    return System.getProperty("os.name").contains("Windows");
  }
  
  /**
   * walk a directory and make sure that files/folders are hidden if they start with a . on windows.
   * 
   * @param startingDir the directory to start walking
   * @throws IOException if there is a problem setting the file/folder to be hidden
   */
  public static void makeFilesHiddenOnWindows(Path startingDir) throws IOException {
    if (isExecutingOnWindows()) {
      Files.walkFileTree(startingDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException{
          String name = dir.getFileName().toString();
          if(name.startsWith(".") && !(name.equals(".keep") || name.equals(".bagit"))){
            Files.setAttribute(dir, "dos:hidden", Boolean.TRUE);
          }
          return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
          String name = path.getFileName().toString();
          if(name.startsWith(".") && !(name.equals(".keep") || name.equals(".bagit"))){
            Files.setAttribute(path, "dos:hidden", Boolean.TRUE);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }
}
