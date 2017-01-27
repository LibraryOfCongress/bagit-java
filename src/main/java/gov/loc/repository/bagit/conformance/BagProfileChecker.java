package gov.loc.repository.bagit.conformance;

import gov.loc.repository.bagit.domain.Bag;

public final class BagProfileChecker {

  private BagProfileChecker(){
    //intentionally left empty
  }
  
  /**
   * Check a bag against a bagit-profile as described by {@link https://github.com/ruebot/bagit-profiles}
   * 
   * @param jsonProfile the json string describing the profile
   * @param bag the bag to check against the profile
   * 
   * @return true if the bag conforms to the bagit profile, false otherwise
   */
  public static boolean bagConformsToProfile(final String jsonProfile, final Bag bag){
    //TODO
    return false;
  }
}
