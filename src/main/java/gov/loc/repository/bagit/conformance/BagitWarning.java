package gov.loc.repository.bagit.conformance;

/**
 * The BagIt specification is very flexible in what it allows. 
 * This leads to situations where something may be technically allowed, but should be discouraged. 
 * This class is for that purpose, to allow reporting of these allowed but discouraged situations to the user.
 */
public enum BagitWarning {
  BAG_WITHIN_A_BAG("A data directory can contain anything,"
      + " including another bag. However it would be better to merge the bags together."),
  LEADING_DOT_SLASH("A manifest lists all data files as relative to the bag root directory,"
      + " it is superfluous to therefore specify it with a dot."),
  PAYLOAD_OXUM_MISSING("It is recommended to always include the Payload-Oxum in the bag metadata "
      + "since it allows for a 'quick verification' of the bag."),
  WEAK_CHECKSUM_ALGORITHM("The checksum algorithm used is known to be weak. We recommend using SHA512 at a minium"),
  NON_STANDARD_ALGORITHM("The checksum algorithm used does not come standard with the Java runtime. Consider using SHA512 instead."),
  TAG_FILES_ENCODING("It is recommended to always use UTF-8"),
  OLD_BAGIT_VERSION("The bagit specification version is not the newest. Consider converting to the latest version.");
  
  private final String reason;
  
  private BagitWarning(final String reason){
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }
}
