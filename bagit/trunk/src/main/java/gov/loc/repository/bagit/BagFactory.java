package gov.loc.repository.bagit;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;

import java.io.File;

public class BagFactory {	
	
	public enum LoadOption { NO_LOAD, BY_PAYLOAD_MANIFESTS, BY_PAYLOAD_FILES }
	
	public enum Version { V0_95 ("0.95"), V0_96 ("0.96");
	
	public String versionString;
	
	Version(String versionString) {
		this.versionString = versionString;
	}
	
	public static Version valueOfString(String versionString) {
		for(Version version : Version.values()) {
			if(version.versionString.equals(versionString)) {
				return version;
			}
		}
		throw new IllegalArgumentException();
	}
	
	}
	
	public static final Version LATEST = Version.V0_96;
	
	public BagFactory() {
		
	}
		
	/**
	 * Creates a new Bag of the latest version.
	 */
	public Bag createBag() {
		return createBag(LATEST);
	}
	
	/**
	 * Creates a new Bag of the specified version.
	 */
	public Bag createBag(Version version) {
		if (Version.V0_95.equals(version)) {
			return new gov.loc.repository.bagit.v0_95.impl.BagImpl(this);
		}
		if (Version.V0_96.equals(version)) {
			return new gov.loc.repository.bagit.v0_96.impl.BagImpl(this);
		}
		throw new RuntimeException("Not yet supported");
	}
	
	/**
	 * Creates a Bag from an existing bag.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the specified version is not supported, the latest version is used.
	 * The bag is loaded from the payload manifests.
	 */
	public Bag createBag(File bagFile) {
		return createBag(bagFile, LoadOption.BY_PAYLOAD_MANIFESTS);
	}

	/**
	 * Creates a Bag from an existing bag.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the specified version is not supported, the latest version is used.
	 */
	public Bag createBag(File bagFile, LoadOption loadOption) {
		String versionString = BagHelper.getVersion(bagFile);
		Version version = LATEST;
		if (versionString != null) {
			for(Version v : Version.values()) {
				if(v.versionString.equals(versionString)) {
					version = v;
				}
			}
		}
		return createBag(bagFile, version, loadOption);

	}

	
	/**
	 * Creates a Bag from an existing bag using the specified version.
	 */
	public Bag createBag(File bagFile, Version version, LoadOption loadOption) {		
		Bag bag = this.createBag(version);
		bag.setFile(bagFile);
		if (loadOption == null || LoadOption.BY_PAYLOAD_MANIFESTS.equals(loadOption)) {
			bag.loadFromPayloadManifests();
		} else if (LoadOption.BY_PAYLOAD_FILES.equals(loadOption)) {
			bag.loadFromPayloadFiles();
		}
		return bag;
	}

	/**
	 * Creates a Bag from an existing Bag.
	 * The version and bagFile (if present) are taken from the existing Bag.
	 * The bag is not loaded.
	 */
	public Bag createBag(Bag bag) {
		if (bag.getFile() == null) {
			return createBag(bag.getBagConstants().getVersion());
		}
		return createBag(bag.getFile(), bag.getBagConstants().getVersion(), LoadOption.NO_LOAD);
	}
	
	
	/**
	 * Gets a BagPartFactory of the latest version.
	 */
	public BagPartFactory getBagPartFactory() {
		return getBagPartFactory(LATEST);
	}
	
	/**
	 * Gets a BagPartFactory of the specified version.
	 */
	public BagPartFactory getBagPartFactory(Version version) {
		if (Version.V0_95.equals(version)) {
			return new gov.loc.repository.bagit.v0_95.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
		}
		if (Version.V0_96.equals(version)) {
			return new gov.loc.repository.bagit.v0_96.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
		}
		throw new RuntimeException("Not yet supported");
	}
	
	/**
	 * Gets BagConstants of the latest version.
	 */
	public BagConstants getBagConstants() {
		return getBagConstants(LATEST);
	}
	
	/**
	 * Gets BagConstants of the specified version.
	 */
	public BagConstants getBagConstants(Version version) {
		if (Version.V0_95.equals(version)) {
			return new gov.loc.repository.bagit.v0_95.impl.BagConstantsImpl();
		}
		if (Version.V0_96.equals(version)) {
			return new gov.loc.repository.bagit.v0_96.impl.BagConstantsImpl();
		}
		throw new RuntimeException("Not yet supported");
	}	
	
}
