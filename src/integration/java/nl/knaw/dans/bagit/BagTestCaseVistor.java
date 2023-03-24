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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class BagTestCaseVistor extends SimpleFileVisitor<Path>{
  private static final Path INVALID_DIR_NAME = Paths.get("invalid");
  private static final Path VALID_DIR_NAME = Paths.get("valid");
  private static final Path WARNING_DIR_NAME = Paths.get("warning");
  private static final Path WINDOWS_DIR_NAME = Paths.get("windows-only");
  private static final Path LINUX_DIR_NAME = Paths.get("linux-only");
  
  private final List<Path> invalidTestCases = new ArrayList<>();
  private final List<Path> validTestCases = new ArrayList<>();
  private final List<Path> warningTestCases = new ArrayList<>();
  private final List<Path> windowsOnlyTestCases = new ArrayList<>();
  private final List<Path> linuxOnlyTestCases = new ArrayList<>();
  
  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
    if(dir.getParent() != null){
      if(dir.getParent().getFileName().startsWith(INVALID_DIR_NAME)){
        invalidTestCases.add(dir);
      }
      if(dir.getParent().getFileName().startsWith(VALID_DIR_NAME)){
        validTestCases.add(dir);
      }
      if(dir.getParent().getFileName().startsWith(WARNING_DIR_NAME)){
        warningTestCases.add(dir);
      }
      if(dir.getParent().getFileName().startsWith(WINDOWS_DIR_NAME)){
        windowsOnlyTestCases.add(dir);
      }
      if(dir.getParent().getFileName().startsWith(LINUX_DIR_NAME)){
        linuxOnlyTestCases.add(dir);
      }
    }
    
    return FileVisitResult.CONTINUE;
  }

  public List<Path> getInvalidTestCases() {
    return invalidTestCases;
  }

  public List<Path> getValidTestCases() {
    return validTestCases;
  }

  public List<Path> getWarningTestCases() {
    return warningTestCases;
  }

  public List<Path> getWindowsOnlyTestCases() {
    return windowsOnlyTestCases;
  }

  public List<Path> getLinuxOnlyTestCases() {
    return linuxOnlyTestCases;
  }
}
