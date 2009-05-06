package gov.loc.repository.bagit;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public interface Bag {
	
	enum Format {
		ZIP ("zip", true, ".zip"), TAR ("tar", true, ".tar"), TAR_GZ ("tgz", true, ".tar.gz"), TAR_BZ2 ("tbz2", true, ".tar.bz2"), FILESYSTEM ("file", false, ""), VIRTUAL (null, false, null);
		public String scheme;
		public boolean isSerialized;
		public String extension;
		
		Format(String scheme, boolean isSerialized, String extension) {
			this.scheme = scheme;
			this.isSerialized = isSerialized;
			this.extension = extension;
		}
	};
	
	File getFile();
	
	List<Manifest> getPayloadManifests();

	List<Manifest> getTagManifests();
		
	Collection<BagFile> getTags();

	Collection<BagFile> getPayload();
	
	void removeBagFile(String filepath);
	
	BagFile getBagFile(String filepath);
	
	void putBagFile(BagFile bagFile);

	void putBagFiles(Collection<BagFile> bagFiles);
	
	void addFilesToPayload(File file);
	
	void addFilesToPayload(List<File> files);
	
	void addFileAsTag(File file);
	
	BagItTxt getBagItTxt();
	
	BagInfoTxt getBagInfoTxt();
	
	FetchTxt getFetchTxt();
		
	Format getFormat();
	
	/*
	 * Determines whether the bag is valid according to the BagIt Specification.
	 * @param	missingBagItTolerant	whether to allow a valid bag to be missing a BagIt.txt
	 */
	SimpleResult checkValid(boolean missingBagItTolerant);

	/*
	 * Determines whether the bag is valid according to the BagIt Specification.
	 */
	SimpleResult checkValid();

	/*
	 * Determines whether the bag is complete according to the BagIt Specification.
	 * @param	missingBagItTolerant	whether to allow a complete bag to be missing a BagIt.txt
	 */	
	SimpleResult checkComplete(boolean missingBagItTolerant);

	/*
	 * Determines whether the bag is complete according to the BagIt Specification.
	 */		
	SimpleResult checkComplete();

	/*
	 * Additional checks of a bag.
	 * These checks are not specified by the BagIt Specification.
	 * @param	strategies	a list of strategies to invoke
	 */
	SimpleResult checkAdditionalVerify(List<VerifyStrategy> strategies);

	/*
	 * Additional checks of a bag.
	 * These checks are not specified by the BagIt Specification.
	 * @param	strategies	a strategy to invoke
	 */	
	SimpleResult checkAdditionalVerify(VerifyStrategy strategy);
	
	/*
	 * Verify that each checksum in every payload manifest can be verified against
	 * the appropriate contents.
	 */
	SimpleResult checkPayloadManifests();

	/*
	 * Verify that each checksum in every tag manifest can be verified against
	 * the appropriate contents.
	 */	
	SimpleResult checkTagManifests();
			
	void load();
	
	void accept(BagVisitor visitor);
			
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
		Manifest createManifest(String name, Bag bag);
		Manifest createManifest(String name, Bag bag, BagFile sourceBagFile);
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
		FetchTxt createFetchTxt(Bag bag);
		FetchTxt createFetchTxt(Bag bag, BagFile sourceBagFile);
		Version getVersion();
		
	}
}