package gov.loc.repository.bagit.exceptions.conformance;

import java.util.List;

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when a metadata's value is not in the acceptable list of values
 */
public class MetatdataValueIsNotAcceptableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public MetatdataValueIsNotAcceptableException(final String message, final String metadataKey, final List<String> acceptableValues, final String actualValue) {
    super(MessageFormatter.arrayFormat(message, new Object[]{metadataKey, acceptableValues, actualValue}).getMessage());
  }
}
