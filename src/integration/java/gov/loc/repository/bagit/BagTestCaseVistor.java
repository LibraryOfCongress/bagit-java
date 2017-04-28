package gov.loc.repository.bagit;

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
