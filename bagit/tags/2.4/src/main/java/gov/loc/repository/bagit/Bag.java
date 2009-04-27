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
		
	List<Manifest> getPayloadManifests();

	List<Manifest> getTagManifests();
		
	Collection<BagFile> getTagFiles();
	
	void removeTagFile(String filepath);
	
	Collection<BagFile> getPayloadFiles();
	
	BagFile getTagFile(String filepath);
	
	void putTagFile(BagFile bagFile);
	
	BagFile getPayloadFile(String filepath);
	
	void removePayloadFile(String filepath);
	
	void putPayloadFile(BagFile bagFile);
	
	BagItTxt getBagItTxt();
	
	void setBagItTxt(BagItTxt bagItFile);
	
	BagInfoTxt getBagInfoTxt();
	
	void setBagInfoTxt(BagInfoTxt bagInfoTxt);
	
	void setBagInfoTxt(File bagInfoTxtFile);
	
	Format getFormat();
	
	/*
	 * Determines whether the bag is valid according to the BagIt Specification.
	 * @param	missingBagItTolerant	whether to allow a valid bag to be missing a BagIt.txt
	 */
	SimpleResult isValid(boolean missingBagItTolerant);

	/*
	 * Determines whether the bag is valid according to the BagIt Specification.
	 */
	SimpleResult isValid();

	/*
	 * Determines whether the bag is complete according to the BagIt Specification.
	 * @param	missingBagItTolerant	whether to allow a complete bag to be missing a BagIt.txt
	 */	
	SimpleResult isComplete(boolean missingBagItTolerant);

	/*
	 * Determines whether the bag is complete according to the BagIt Specification.
	 */		
	SimpleResult isComplete();

	/*
	 * Additional checks of a bag.
	 * These checks are not specified by the BagIt Specification.
	 * @param	strategies	a list of strategies to invoke
	 */
	SimpleResult additionalVerify(List<VerifyStrategy> strategies);

	/*
	 * Additional checks of a bag.
	 * These checks are not specified by the BagIt Specification.
	 * @param	strategies	a strategy to invoke
	 */	
	SimpleResult additionalVerify(VerifyStrategy strategy);
	
	/*
	 * Verify that each checksum in every payload manifest can be verified against
	 * the appropriate contents.
	 */
	SimpleResult verifyPayloadManifests();

	/*
	 * Verify that each checksum in every tag manifest can be verified against
	 * the appropriate contents.
	 */	
	SimpleResult verifyTagManifests();
	
	void addPayload(File file);
	
	void addPayload(List<File> files);
	
	/*
	 * Fill in the missing parts of a bag so that it is complete.
	 * Uses the DefaultCompletionStrategy.
	 */
	void complete();
	
	/*
	 * Fill in the missing parts of a bag so that it is complete.
	 * @param	strategy	the strategy to be used to complete the bag
	 */
	void complete(CompletionStrategy strategy);
	
	/*
	 * Write the bag.
	 * @param	writer	the writer to write to
	 */
	void write(BagWriter writer);
	
	/*
	 * Make the bag holey.
	 * The involves creating a fetch.txt and removing the payload
	 * @param	baseUrl	the url part to prepend to create the payload url
	 * @param	whether to include the payload directory ("data") in the payload url
	 */
	void makeHoley(String baseUrl, boolean includePayloadDirectory);
	
	FetchTxt getFetchTxt();
	
	void putFetchTxt(FetchTxt fetchTxt);
	
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