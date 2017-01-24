package gov.loc.repository.bagit.exceptions;

import gov.loc.repository.bagit.domain.Bag;

/**
 * The {@link Bag} object should contain the Payload-Oxum metatdata key value pair, 
 * this class represents the error when trying to calculate the payload-oxum and it doesn't exist on the bag object.
 * Or if the payload-byte-count and payload-file-count don't exist for versions 1.0+
 */
public class PayloadOxumDoesNotExistException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PayloadOxumDoesNotExistException(final String message){
    super(message);
  }
}
