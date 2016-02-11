package gov.loc.repository.bagit.exceptions;

public class MissingPayloadManifestException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingPayloadManifestException(String message){
    super(message);
  }
}
