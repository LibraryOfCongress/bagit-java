package gov.loc.repository.bagit.tools;

import gov.loc.repository.bagit.domain.Bag;

/**
 * Responsible for verifying if a bag is valid, complete
 */
public class Verifier {

  /**
   *  See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a></br>
   *  A bag is <b>valid</b> if the bag is complete and every checksum has been 
   *  verified against the contents of its corresponding file.
   */
  public static boolean isValid(Bag bag){
    //TODO
    return false;
  }
  
  /**
   * See <a href="https://tools.ietf.org/html/draft-kunze-bagit-13#section-3">https://tools.ietf.org/html/draft-kunze-bagit-13#section-3</a></br>
   * A bag is <b>complete</b> if </br>
   * <p><ul>
   * <li>every element is present
   * <li>every file in the payload manifest(s) are present
   * <li>every file in the tag manifest(s) are present. Tag files not listed in a tag manifest may be present.
   * <li>every file in the data directory must be listed in at least one payload manifest
   * <li>each element must comply with the bagit spec
   * </ul></p>
   */
  public static boolean isComplete(Bag bag){
    //TODO
    //bagit.txt exists
    // the /data directory exists
    // at least one manifest-<alg>.txt
    
    //for each manifest - file exists
    //for each file in /data dir it must be in at least one manifest
    return false;
  }
}
