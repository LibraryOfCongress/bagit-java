package gov.loc.repository.bagit.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import gov.loc.repository.bagit.domain.VerifyResponse;

public class PayloadFileExistsInManifestVistor extends SimpleFileVisitor<Path> {
  private final Set<File> filesListedInManifests;
  private final VerifyResponse response;

  public PayloadFileExistsInManifestVistor(Set<File> filesListedInManifests, VerifyResponse response) {
    this.filesListedInManifests = filesListedInManifests;
    this.response = response;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)throws IOException{
    if(Files.isRegularFile(path) && !filesListedInManifests.contains(path.toFile())){
      response.setErrored(true);
      response.getErrorMessages().add("File " + path + " is in the payload directory but isn't listed in any of the manifests!");
    }
    return FileVisitResult.CONTINUE;
  }
}
