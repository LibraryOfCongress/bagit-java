package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.BagVerifyResult;

import java.util.List;

public interface ManifestVerifier {
    BagVerifyResult verify(Manifest manifest, Bag bag);
    
    BagVerifyResult verify(List<Manifest> manifests, Bag bag);

}
