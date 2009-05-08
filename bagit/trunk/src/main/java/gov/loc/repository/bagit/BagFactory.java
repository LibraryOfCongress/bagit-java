package gov.loc.repository.bagit;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.impl.ConfigurableBag;

import java.io.File;

public class BagFactory {	
	
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
		
	/*
	 * Creates a new Bag of the latest version.
	 */
	public Bag createBag() {
		return createBag(LATEST);
	}
	
	/*
	 * Creates a new Bag of the specified version.
	 */
	public Bag createBag(Version version) {
		return new ConfigurableBag(this.getBagPartFactory(version), this.getBagConstants(version));
	}
	
	/*
	 * Creates a Bag from an existing bag.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the specified version is not supported, the latest version is used.
	 * The bag is loaded.
	 * @param file either the bag_dir of a bag on the file system or a serialized bag (zip, tar)
	 */
	public Bag createBag(File bagFile) {
		return createBag(bagFile, true);
	}

	/*
	 * Creates a Bag from an existing bag.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the specified version is not supported, the latest version is used.
	 * @param file either the bag_dir of a bag on the file system or a serialized bag (zip, tar)
	 * @param boolean whether to load the bag
	 */
	public Bag createBag(File bagFile, boolean load) {
		String versionString = BagHelper.getVersion(bagFile);
		Version version = LATEST;
		if (versionString != null) {
			for(Version v : Version.values()) {
				if(v.versionString.equals(versionString)) {
					version = v;
				}
			}
		}
		return createBag(bagFile, version, load);

	}

	
	/*
	 * Creates a Bag from an existing bag using the specified version.
	 * @param file either the bag_dir of a bag on the file system or a serialized bag (zip, tar)
	 * @param version
	 * @param boolean whether to load the bag
	 */
	public Bag createBag(File bagFile, Version version, boolean load) {		
		Bag bag = this.createBag(version);
		bag.setFile(bagFile);
		if (load) {
			bag.load();
		}
		return bag;
	}

	/*
	 * Creates a Bag from an existing Bag.
	 * The version and bagFile (if present) are taken from the existing Bag.
	 * @param Bag the Bag to base the new Bag on
	 */
	public Bag createBag(Bag bag) {
		if (bag.getFile() == null) {
			return createBag(bag.getBagConstants().getVersion());
		}
		return createBag(bag.getFile(), bag.getBagConstants().getVersion(), false);
	}
	
	
	/*
	 * Gets a BagPartFactory of the latest version.
	 */
	public BagPartFactory getBagPartFactory() {
		return getBagPartFactory(LATEST);
	}
	
	/*
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
	
	/*
	 * Gets BagConstants of the latest version.
	 */
	public BagConstants getBagConstants() {
		return getBagConstants(LATEST);
	}
	
	/*
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
