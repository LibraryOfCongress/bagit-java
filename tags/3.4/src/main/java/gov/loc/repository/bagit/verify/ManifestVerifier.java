package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.util.List;

public interface ManifestVerifier {
    SimpleResult verify(Manifest manifest, Bag bag);
    
    SimpleResult verify(List<Manifest> manifests, Bag bag);

}
