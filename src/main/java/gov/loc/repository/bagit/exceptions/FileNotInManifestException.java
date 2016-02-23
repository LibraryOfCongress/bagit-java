package gov.loc.repository.bagit.exceptions;

import java.io.IOException;

/**
 * Class to represent an error when a file is found in the payload directory but not in any manifest.
 * Opposite to {@link FileNotInPayloadDirectoryException}
 */
public class FileNotInManifestException extends IOException {
  private static final long serialVersionUID = 1L;

  public FileNotInManifestException(String message){
    super(message);
  }
}
