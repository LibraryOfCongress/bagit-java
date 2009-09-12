package gov.loc.repository.bagit.impl;

import gov.loc.repository.bagit.Bag.BagConstants;;

public abstract class AbstractBagConstants implements BagConstants {
	public static final String PAYLOAD_MANIFEST_PREFIX = "manifest-";
	public static final String TAG_MANIFEST_PREFIX = "tagmanifest-";
	public static final String PAYLOAD_MANIFEST_SUFFIX = ".txt";
	public static final String TAG_MANIFEST_SUFFIX = ".txt";
	public static final String BAG_ENCODING = "UTF-8";
	public static final String BAGIT_TXT = "bagit.txt";
	public static final String DATA_DIRECTORY = "data";
	public static final String BAGINFO_TXT = "bag-info.txt";
	public static final String FETCH_TXT = "fetch.txt";
	
	public String getPayloadManifestPrefix() {
		return PAYLOAD_MANIFEST_PREFIX;
	}
	public String getTagManifestPrefix() {
		return TAG_MANIFEST_PREFIX;
	}
	public String getPayloadManifestSuffix() {
		return PAYLOAD_MANIFEST_SUFFIX;
	}
	public String getTagManifestSuffix() {
		return TAG_MANIFEST_SUFFIX;
	}
	public String getBagEncoding() {
		return BAG_ENCODING;
	}
	public String getBagItTxt() {
		return BAGIT_TXT;
	}
	public String getDataDirectory() {
		return DATA_DIRECTORY;
	}
	
	public String getBagInfoTxt() {
		return BAGINFO_TXT;
	}
	
	public String getFetchTxt() {
		return FETCH_TXT;
	}
}
