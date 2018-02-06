package gov.loc.repository.bagit.exceptions.conformance;

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when a metadata's value is not to be repeated
 */
public class MetatdataValueIsNotRepeatableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public MetatdataValueIsNotRepeatableException(final String message, final String metadataKey) {
    super(MessageFormatter.format(message, metadataKey).getMessage());
  }
}
