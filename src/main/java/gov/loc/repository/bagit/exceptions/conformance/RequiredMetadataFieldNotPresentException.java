package gov.loc.repository.bagit.exceptions.conformance;

import org.slf4j.helpers.MessageFormatter;

/**
 * Class to represent when a specific metadata field is not found
 */
public class RequiredMetadataFieldNotPresentException extends Exception {
private static final long serialVersionUID = 1L;
  
  public RequiredMetadataFieldNotPresentException(final String message, final String bagInfoEntryRequirementKey) {
    super(MessageFormatter.format(message, bagInfoEntryRequirementKey).getMessage());
  }
}
