package gov.loc.repository.bagit.exceptions;

/**
 * A bagit bag needs at least one payload manifest. This class represents the error if at least one payload manifest isn't found.
 */
public class MissingPayloadManifestException extends Exception {
  private static final long serialVersionUID = 1L;

  public MissingPayloadManifestException(String message){
    super(message);
  }
}
