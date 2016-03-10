package gov.loc.repository.bagit.exceptions;

/**
 * Class to represent an error when a file is not in the payload directory but is listed in a manifest.
 * Opposite to {@link FileNotInManifestException}
 */
public class FileNotInPayloadDirectoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public FileNotInPayloadDirectoryException(String message){
    super(message);
  }
}
