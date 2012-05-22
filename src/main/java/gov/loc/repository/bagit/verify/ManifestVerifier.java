package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.util.List;

public interface ManifestVerifier {
	
	public static final String CODE_PAYLOAD_MANIFEST_CONTAINS_INVALID_FILE = "payload_manifest_contains_invalid_file";
	public static final String CODE_TAG_MANIFEST_CONTAINS_INVALID_FILE = "tag_manifest_contains_invalid_file";
	
	SimpleResult verify(Manifest manifest, Bag bag);
    
	SimpleResult verify(List<Manifest> manifests, Bag bag);

}
