package gov.loc.repository.bagit;

import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.impl.PreBagImpl;

import java.io.File;
import java.util.List;

/**
 * <p>Provides all methods for instantiating new {@link Bag} objects, as well
 * as reading bags from disk and serializations.  You should not create a
 * Bag instance directly, instead creating them with methods from this
 * class.</p>
 * 
 * <p>New in-memory bags can be created using the
 * {@link #createBag() no-argument createBag()} method, while clones of an
 * existing bag can be created using the {@link #createBag(Bag)} method.
 * A bag on-disk can be loaded using the {@link #createBag(File)} method.</p>
 * 
 * <p>Additionally, there are overloads for specifying the
 * {@link Version} and the {@link LoadOption LoadOptions}.</p> 
 *
 * @see Bag
 */
public class BagFactory {	
	
	/**
	 * <p>Specifies the mechanism used to load a bag from disk.
	 * The mechanism used to load the bag will depend on the
	 * type of operations being performed.  For example, when
	 * creating a new bag based on an existing data directory,
	 * the {@link #BY_FILES} option would be used;
	 * but when loading a bag for simple verification of completeness
	 * and content, one would use the {@link #BY_MANIFESTS} option.
	 */
	public enum LoadOption {
		/**
		 * Does not load the bag.
		 */
		NO_LOAD, 
		
		/**
		 * Loads the bag by reading from the manifests.
		 */
		BY_MANIFESTS,
		
		/**
		 * Loads the bag by reading from the files on disk.
		 */
		BY_FILES 
	}
	
	/**
	 * The version of the bag to load.  The BagIt Library does not support any
	 * bag versions other than those listed here.
	 */
	public enum Version { V0_93 ("0.93"), V0_94 ("0.94"), V0_95 ("0.95"), V0_96 ("0.96"), V0_97 ("0.97");
	
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
	
	/**
	 * The latest version of the BagIt spec.  Currently, this
	 * is {@link Version#V0_97 0.97}.
	 */
	public static final Version LATEST = Version.V0_97;
	
	/**
	 * Creates an instance of a bag factory.
	 */
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
	 * @param version The version of the bag to be created.
	 * @throws RuntimeException Thrown if an unsupported version is passed.
	 */
	public Bag createBag(Version version) {
		if (Version.V0_93.equals(version)) {
			return new gov.loc.repository.bagit.v0_93.impl.BagImpl(this);
		}
		if (Version.V0_94.equals(version)) {
			return new gov.loc.repository.bagit.v0_94.impl.BagImpl(this);
		}
		if (Version.V0_95.equals(version)) {
			return new gov.loc.repository.bagit.v0_95.impl.BagImpl(this);
		}
		if (Version.V0_96.equals(version)) {
			return new gov.loc.repository.bagit.v0_96.impl.BagImpl(this);
		}
		if (Version.V0_97.equals(version)) {
			return new gov.loc.repository.bagit.v0_97.impl.BagImpl(this);
		}
		
		throw new RuntimeException("Not yet supported");
	}
	
	/**
	 * Creates a Bag from an existing bag.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the specified version is not supported, the latest version is used.
	 * The bag is loaded from the payload manifests.
	 * 
	 * @param bagFile The {@link File} from which to load the bag.  This may
	 * be either a filesystem directory, or a file containing a serialized
	 * bag.
	 */
	public Bag createBag(File bagFile) {
		return createBag(bagFile, LoadOption.BY_MANIFESTS);
	}

	/**
	 * Creates a Bag from an existing bag.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the version of the bag is not supported by this library,
	 * the latest version is used.
	 * 
	 * @param bagFile The {@link File} containing the bag to load.
	 * @param loadOption The mechanism to use for loading the bag.
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
	 * Creates a Bag from an existing bag loading from the actual files.
	 * The version of the bag is determined by examining the bag.
	 * If it cannot be determined, the latest version is assumed.
	 * If the version of the bag is not supported by this library,
	 * the latest version is used.
	 * 
	 * @param bagFile The {@link File} containing the bag to load.
	 * @param version The version to load the bag as.   
	 * @param ignoreAdditionalDirecories A set of directories to ignore when
	 * loading from the actual files.
	 */
	public Bag createBagByPayloadFiles(File bagFile, Version version, List<String> ignoreAdditionalDirectories) {
		Bag bag = this.createBag(version);
		bag.setFile(bagFile);
		bag.loadFromFiles(ignoreAdditionalDirectories);
		return bag;
	}

	
	
	/**
	 * Creates a Bag from an existing bag using the specified version.

	 * @param bagFile The {@link File} containing the bag to load.
	 * @param version The version to load the bag as.   
	 * @param loadOption The mechanism to use for loading the bag.
	 */
	public Bag createBag(File bagFile, Version version, LoadOption loadOption) {		
		Bag bag = this.createBag(version);
		bag.setFile(bagFile);
		if (loadOption == null || LoadOption.BY_MANIFESTS.equals(loadOption)) {
			bag.loadFromManifests();
		} else if (LoadOption.BY_FILES.equals(loadOption)) {
			bag.loadFromFiles();
		}
		return bag;
	}

	/**
	 * Creates a Bag from an existing Bag.
	 * The version and bagFile (if present) are taken from the existing Bag.
	 * The bag is not loaded.
	 * 
	 * @param bag The bag to copy.
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
	 * 
	 * @param version The version for which to retrieve a {@link BagPartFactory}.
	 */
	public BagPartFactory getBagPartFactory(Version version) {
		if (Version.V0_93.equals(version)) {
			return new gov.loc.repository.bagit.v0_93.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
		}
		if (Version.V0_94.equals(version)) {
			return new gov.loc.repository.bagit.v0_94.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
		}
		if (Version.V0_95.equals(version)) {
			return new gov.loc.repository.bagit.v0_95.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
		}
		if (Version.V0_96.equals(version)) {
			return new gov.loc.repository.bagit.v0_96.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
		}
		if (Version.V0_97.equals(version)) {
			return new gov.loc.repository.bagit.v0_97.impl.BagPartFactoryImpl(this, this.getBagConstants(version));
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
	 * 
	 * @param version The version for which to retrieve a {@link BagConstants}.
	 */
	public BagConstants getBagConstants(Version version) {
		if (Version.V0_93.equals(version)) {
			return new gov.loc.repository.bagit.v0_93.impl.BagConstantsImpl();
		}
		if (Version.V0_94.equals(version)) {
			return new gov.loc.repository.bagit.v0_94.impl.BagConstantsImpl();
		}
		if (Version.V0_95.equals(version)) {
			return new gov.loc.repository.bagit.v0_95.impl.BagConstantsImpl();
		}
		if (Version.V0_96.equals(version)) {
			return new gov.loc.repository.bagit.v0_96.impl.BagConstantsImpl();
		}
		if (Version.V0_97.equals(version)) {
			return new gov.loc.repository.bagit.v0_97.impl.BagConstantsImpl();
		}
		throw new RuntimeException("Not yet supported");
	}
	
	/**
	 * Creates a PreBag which can be bagged-in-place.
	 * @param dir The {@link File} containing the data to be pre-bagged.
	 */
	public PreBag createPreBag(File dir) {
		PreBag preBag = new PreBagImpl(this);
		preBag.setFile(dir);
		return preBag;
	}
	
}
