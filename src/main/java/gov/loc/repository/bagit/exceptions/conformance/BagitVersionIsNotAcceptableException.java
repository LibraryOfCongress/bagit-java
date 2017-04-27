package gov.loc.repository.bagit.exceptions.conformance;

import java.util.List;

import org.slf4j.helpers.MessageFormatter;

import gov.loc.repository.bagit.domain.Version;

/**
 * Class to represent when the bag's version is not in the acceptable list of versions
 */
public class BagitVersionIsNotAcceptableException extends Exception {
private static final long serialVersionUID = 1L;
  
  public BagitVersionIsNotAcceptableException(final String message, final Version version, final List<String> acceptableVersions) {
    super(MessageFormatter.format(message, version, acceptableVersions).getMessage());
  }
}
