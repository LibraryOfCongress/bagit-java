package gov.loc.repository.bagit.verify;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.utilities.SimpleResult;


public interface CompleteVerifier {
	public static final String CODE_NO_PAYLOAD_MANIFEST = "no_payload_manifest";
	public static final String CODE_NO_BAGITTXT = "no_bagittxt";
	public static final String CODE_WRONG_VERSION = "wrong_version";
	public static final String CODE_PAYLOAD_NOT_IN_PAYLOAD_DIRECTORY = "payload_not_in_payload_directory";
	public static final String CODE_TAG_IN_PAYLOAD_MANIFEST = "tag_in_payload_manifest";
	public static final String CODE_DIRECTORY_NOT_ALLOWED_IN_BAG_DIR = "directory_not_allowed_in_bag_dir";
	public static final String CODE_PAYLOAD_FILE_NOT_IN_PAYLOAD_MANIFEST = "payload_file_not_in_payload_manifest";
	public static final String CODE_PAYLOAD_MANIFEST_CONTAINS_MISSING_FILE = "payload_manifest_contains_missing_file";
	public static final String CODE_TAG_MANIFEST_CONTAINS_MISSING_FILE = "tag_manifest_contains_missing_file";
	
	SimpleResult verify(Bag bag);
	
}
