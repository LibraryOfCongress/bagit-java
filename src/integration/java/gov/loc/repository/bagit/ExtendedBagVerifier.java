package gov.loc.repository.bagit;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.exceptions.VerificationException;
import gov.loc.repository.bagit.verify.BagVerifier;

import java.io.IOException;

// an example of extending the BagVerifier in order to add your own checks that suffice the specific
// verification rules for a bag, as defined by your project
// NOTE: this cannot override the BagVerifier.isComplete or BagVerifier.isValid, as they're defined
//       by "The BagIt File Packaging Format" (https://tools.ietf.org/html/draft-kunze-bagit).
public class ExtendedBagVerifier extends BagVerifier {

  public void isValidAccordingToMyValidation(final Bag bag, final boolean ignoreHiddenFiles)
      throws InterruptedException, CorruptChecksumException, VerificationException,
      MaliciousPathException, FileNotInPayloadDirectoryException, UnsupportedAlgorithmException,
      InvalidBagitFileFormatException, IOException {

    for (Manifest manifest : bag.getPayLoadManifests()) {
      this.checkHashes(manifest);
    }

    getManifestVerifier().verifyPayload(bag, ignoreHiddenFiles);
  }
}
