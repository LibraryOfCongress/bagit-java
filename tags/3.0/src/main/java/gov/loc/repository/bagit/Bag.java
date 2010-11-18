package gov.loc.repository.bagit;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.Verifier;
import gov.loc.repository.bagit.writer.Writer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Bag {
	
	enum Format {
		ZIP ("zip", true, ".zip"), TAR ("tar", true, ".tar"), TAR_GZ ("tgz", true, ".tar.gz"), TAR_BZ2 ("tbz2", true, ".tar.bz2"), FILESYSTEM ("file", false, "");
		public String scheme;
		public boolean isSerialized;
		public String extension;
		
		Format(String scheme, boolean isSerialized, String extension) {
			this.scheme = scheme;
			this.isSerialized = isSerialized;
			this.extension = extension;
		}
		
		
	};
	
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
	
	void removePayloadDirectory(String filepath);
	
	BagFile getBagFile(String filepath);
	
	void putBagFile(BagFile bagFile);

	void putBagFiles(Collection<BagFile> bagFiles);
	
	void addFileToPayload(File file);
	
	void addFilesToPayload(List<File> files);
	
	void addFileAsTag(File file);

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

	/**
	 * Determines whether the bag is complete according to the BagIt Specification.
	 */		
	SimpleResult verifyComplete();
	
	/**
	 * Invokes a Verifier to verify a bag.
	 */	
	SimpleResult verify(Verifier verifier);
	
	/**
	 * Verify that each checksum in every payload manifest can be verified against
	 * the appropriate contents.
	 */
	SimpleResult verifyPayloadManifests();
	
	/**
	 * Verify that each checksum in every tag manifest can be verified against
	 * the appropriate contents.
	 */	
	SimpleResult verifyTagManifests();

	/**
	 * Loads a bag based on the tag files found on disk and the payload files listed in the payload manifests.
	 */
	void loadFromPayloadManifests();

	/**
	 * Loads a bag based on the tag files and payload files found on disk.
	 */
	void loadFromPayloadFiles();

	/**
	 * Invokes a BagVisitor.
	 */
	void accept(BagVisitor visitor);
	
	Bag write(Writer writer, File file);

	/**
	 * Makes a bag holey by creating a fetch.txt and removing payload files.
	 */
	Bag makeHoley(String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags);
	
	/**
	 * Invokes a HolePuncher to make a bag holey.
	 */	
	Bag makeHoley(HolePuncher holePuncher, String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags);
	
	/**
	 * Makes a bag complete by filling in any pieces necessary to satisfy the BagIt Specification.
	 */
	Bag makeComplete();

	/**
	 * Invokes a Completer to make a bag complete;
	 */
	Bag makeComplete(Completer completer);
	
	BagConstants getBagConstants();
	
	BagPartFactory getBagPartFactory();
	
	public interface BagConstants {

		String getPayloadManifestPrefix();
		String getTagManifestPrefix();
		String getPayloadManifestSuffix();
		String getTagManifestSuffix();
		String getBagEncoding();
		String getBagItTxt();
		String getDataDirectory();
		String getBagInfoTxt();
		String getFetchTxt();
		Version getVersion();
	}
	
	public interface BagPartFactory {
		ManifestReader createManifestReader(InputStream in, String encoding);
		ManifestReader createManifestReader(InputStream in, String encoding, boolean treatBackSlashAsPathSeparator);
		ManifestWriter createManifestWriter(OutputStream out);
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