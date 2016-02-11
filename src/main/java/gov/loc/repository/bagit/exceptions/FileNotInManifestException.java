package gov.loc.repository.bagit.exceptions;

import java.io.IOException;

public class FileNotInManifestException extends IOException {
  private static final long serialVersionUID = 1L;

  public FileNotInManifestException(String message){
    super(message);
  }
}
