package gov.loc.repository.bagit;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.FailModeSupporting.FailMode;
import gov.loc.repository.bagit.verify.Verifier;
import gov.loc.repository.bagit.writer.Writer;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>This is the core interface of the BagIt Library, representing
 * a bag from the BagIt spec.
 * Methods are available for creating, manipulating, writing, validating,
 * and verifying bags.</p>
 * 
 * <p>You should not create a Bag instance directly.  Instead, use an
 * appropriate method on the {@link BagFactory} class.</p>
 *
 * @see BagFactory
 */
public interface Bag extends Closeable {
	
	/**
	 * <p>The format of a bag.  Bags may be serialized (such
	 * as "zip") or they may simply be directories on
	 * the filesystem (such as "file").</p>
	 * 
	 * <table border="2">
	 * <tbody>
	 * <tr><th>Format</th><th>Scheme</th><th>Extension</th><th>Serialized?</th></tr>
	 * <tr><td>{@link #FILESYSTEM}</td><td>file</td><td>&lt;none&gt;</td><td>false</td></tr>
	 * <tr><td>{@link #ZIP}</td><td>zip</td><td>.zip</td><td>true</td></tr>
	 * </tbody>
	 * </table>
	 */
	enum Format {
		ZIP ("zip", true, ".zip"), FILESYSTEM ("file", false, "");
		
		/**
		 * The URI scheme for the format.
		 */
		public String scheme;
		
		/**
		 * Whether or not the format is a serialized bag format.
		 */
		public boolean isSerialized;
		
		/**
		 * The file extension typicaly appended to a bag name
		 * in the given format when it is written to disk.
		 */
		public String extension;
		
		Format(String scheme, boolean isSerialized, String extension) {
			this.scheme = scheme;
			this.isSerialized = isSerialized;
			this.extension = extension;
		}
		
	};
	
	/**
	 * Gets the version of the BagIt spec to which the bag conforms.
	 * @return The version of the bag.  Will never be null.
	 */
	Version getVersion();
	
	File getFile();

	void setFile(File file);
	
	List<Manifest> getPayloadManifests();
	
	Manifest getPayloadManifest(Algorithm algorithm);

	List<Manifest> getTagManifests();
	
	Manifest getTagManifest(Algorithm algorithm);
		
	Collection<BagFile> getTags();

	Collection<BagFile> getPayload();
	
	void removeBagFile(String filepath);

	void removeTagDirectory(String filepath);
	
	void removePayloadDirectory(String filepath);
	
	BagFile getBagFile(String filepath);
	
	void putBagFile(BagFile bagFile);

	void putBagFiles(Collection<BagFile> bagFiles);
	
	void addFileToPayload(File file);
	
	void addFilesToPayload(List<File> files);
	
	void addFileAsTag(File file);

	void addFilesAsTag(List<File> files);
	
	/**
	 * Finds checksums in all manifests for a file.
	 */
	Map<Algorithm, String> getChecksums(String filepath);
	
	BagItTxt getBagItTxt();
	
	BagInfoTxt getBagInfoTxt();
	
	FetchTxt getFetchTxt();
		
	Format getFormat();

	/**
	 * Determines whether the bag is valid according to the BagIt Specification.
	 */		
	SimpleResult verifyValid();

	SimpleResult verifyValid(FailMode failMode);

	
	/**
	 * Determines whether the bag is complete according to the BagIt Specification.
	 */		
	SimpleResult verifyComplete();

	SimpleResult verifyComplete(FailMode failMode);

	
	/**
	 * Invokes a Verifier to verify a bag.
	 */	
	SimpleResult verify(Verifier verifier);
		
	/**
	 * Verify that each checksum in every payload manifest can be verified against
	 * the appropriate contents.
	 */
	SimpleResult verifyPayloadManifests();

	SimpleResult verifyPayloadManifests(FailMode failMode);

	
	/**
	 * Verify that each checksum in every tag manifest can be verified against
	 * the appropriate contents.
	 */	
	SimpleResult verifyTagManifests();

	SimpleResult verifyTagManifests(FailMode failMode);

	
	/**
	 * Loads a bag based on the tag files found on disk and the payload files listed in the payload manifests.
	 */
	void loadFromManifests();

	/**
	 * Loads a bag based on the tag files and payload files found on disk.
	 */
	void loadFromFiles();

	void loadFromFiles(List<String> ignoreAdditionalDirectories);
	
	/**
	 * Invokes a BagVisitor.
	 */
	void accept(BagVisitor visitor);
	
	Bag write(Writer writer, File file);

	/**
	 * Makes a bag holey by creating a fetch.txt and removing payload files.
	 */
	
	Bag makeHoley(String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, boolean resume);

	/**
	 * Invokes a HolePuncher to make a bag holey.
	 */	
	Bag makeHoley(HolePuncher holePuncher, String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, boolean resume);
	
	/**
	 * Makes a bag complete by filling in any pieces necessary to satisfy the BagIt Specification.
	 */
	Bag makeComplete();

	/**
	 * Invokes a Completer to make a bag complete.
	 */
	Bag makeComplete(Completer completer);
		
	BagConstants getBagConstants();
	
	BagPartFactory getBagPartFactory();
	
	/**
	 * <p>Contains names for constants associated with a bag.
	 * BagIt defines and reserves several names, and some of those names
	 * change between versions of the specification.  This interface
	 * abstracts away those constants so they can be examined on a
	 * per-version basis.</p>
	 * 
	 * <p>For example, the <c>bag-info.txt</c> file was called
	 * <c>package-info.txt</c> in earlier versions of the spec.
	 * The correct name can be determined by using the
	 * {@link #getBagInfoTxt()} method.</p>
	 * 
	 * <p>You should never reference BagIt constants by name directly
	 * in your code, as they may change from version to version.  Instead,
	 * obtain an instance of this interface and its values as the
	 * constants.
	 * Constants for the current bag's version may be obtained by
	 * calling the {@link Bag#getBagConstants()} method.
	 * Constants for a particular BagIt version may be obtained
	 * by calling the {@link BagFactory#getBagConstants(Version)}
	 * method.</p>
	 * 
	 * @see Bag#getBagConstants()
	 * @see BagFactory#getBagConstants()
	 * @see BagFactory#getBagConstants(Version)
	 */
	public interface BagConstants {

		/**
		 * Get the prefix for a payload manifest, "manifest-"
		 * in the latest version.
		 * @return The constant.
		 */
		String getPayloadManifestPrefix();

		/**
		 * Get the prefix for a payload manifest, "tagmanifest-"
		 * in the latest version.
		 * @return The constant.
		 */
		String getTagManifestPrefix();

		/**
		 * Get the prefix for a payload manifest, ".txt"
		 * in the latest version.
		 * @return The constant.
		 */
		String getPayloadManifestSuffix();

		/**
		 * Get the prefix for a payload manifest, ".txt"
		 * in the latest version.
		 * @return The constant.
		 */
		String getTagManifestSuffix();

		/**
		 * Get the text encoding required for the
		 * {@link #getBagItTxt() bagit.txt} file, "UTF-8" in the latest
		 * version.
		 * @return The constant.
		 */
		String getBagEncoding();

		
		/**
		 * Get the name of the bag declaration file, "bagit.txt"
		 * in the latest version.
		 * @return The constant.
		 */
		String getBagItTxt();

		/**
		 * Get the name of the payload directory, "data"
		 * in the latest version.
		 * @return The constant.
		 */
		String getDataDirectory();

		/**
		 * Get the name of the standard bag metdata file, "bag-info.txt"
		 * in the latest version.
		 * @return The constant.
		 */
		String getBagInfoTxt();

		/**
		 * Get the name of the fetch file, "fetch.txt"
		 * in the latest version.
		 * @return The constant.
		 */
		String getFetchTxt();

		/**
		 * Get the version of the spec these constants are for.
		 * @return The version.
		 */
		Version getVersion();
	}

	/**
	 * <p>Creates various parts of a bag, as appropriate for the
	 * version and underlying implementation of the {@link Bag} interface.</p>
	 * 
	 * <p>You should never create implementations for the various components
	 * of a bag directly.  Instead, you should obtain a parts factory through
	 * {@link Bag#getBagPartFactory()} and then create the desired component
	 * through the factory.</p>
	 * 
	 * <p>The components created are not already "bound" to a bag.  They
	 * must still be added to the bag using methods such as
	 * {@link Bag#putBagFile(BagFile)}.</p>
	 * 
	 * @see Bag
	 */
	public interface BagPartFactory {
		ManifestReader createManifestReader(InputStream in, String encoding);
		ManifestReader createManifestReader(InputStream in, String encoding, boolean treatBackSlashAsPathSeparator);
		ManifestWriter createManifestWriter(OutputStream out);
		ManifestWriter createManifestWriter(OutputStream out, String manifestSeparator);
		Manifest createManifest(String name);
		Manifest createManifest(String name, BagFile sourceBagFile);
		BagItTxtReader createBagItTxtReader(String encoding, InputStream in);
		BagItTxtWriter createBagItTxtWriter(OutputStream out, String encoding, int lineLength, int indentSpaces);
		BagItTxtWriter createBagItTxtWriter(OutputStream out, String encoding);
		BagItTxt createBagItTxt(BagFile bagFile);
		BagItTxt createBagItTxt();
		BagInfoTxtReader createBagInfoTxtReader(String encoding, InputStream in);
		BagInfoTxtWriter createBagInfoTxtWriter(OutputStream out, String encoding, int lineLength, int indentSpaces);
		BagInfoTxtWriter createBagInfoTxtWriter(OutputStream out, String encoding);
		BagInfoTxt createBagInfoTxt(BagFile bagFile);
		BagInfoTxt createBagInfoTxt();
		FetchTxtReader createFetchTxtReader(InputStream in, String encoding);
		FetchTxtWriter createFetchTxtWriter(OutputStream out);
		FetchTxt createFetchTxt();
		FetchTxt createFetchTxt(BagFile sourceBagFile);
		Version getVersion();	
	}
		
}