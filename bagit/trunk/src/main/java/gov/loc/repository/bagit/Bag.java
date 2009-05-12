package gov.loc.repository.bagit;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;
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
	
	void addFilesToPayload(File file);
	
	void addFilesToPayload(List<File> files);
	
	void addFileAsTag(File file);

	Map<Algorithm, String> getChecksums(String filepath);
	
	BagItTxt getBagItTxt();
	
	BagInfoTxt getBagInfoTxt();
	
	FetchTxt getFetchTxt();
		
	Format getFormat();
	
	/*
	 * Determines whether the bag is valid according to the BagIt Specification.
	 */
	SimpleResult verifyValid(CancelIndicator cancelIndicator, ProgressListener progressListener);

	/*
	 * Determines whether the bag is valid according to the BagIt Specification.
	 */
	SimpleResult verifyValid();

	/*
	 * Determines whether the bag is complete according to the BagIt Specification.
	 */	
	SimpleResult verifyComplete(CancelIndicator cancelIndicator, ProgressListener progressListener);

	/*
	 * Determines whether the bag is complete according to the BagIt Specification.
	 */		
	SimpleResult verifyComplete();

	/*
	 * @param	verifiers	a list of Verifiers to invoke
	 */
	SimpleResult verify(List<Verifier> verifiers);
	
	/*
	 * Additional checks of a bag.
	 * These checks are not specified by the BagIt Specification.
	 * @param	strategies	a Verifier to invoke
	 */	
	SimpleResult verify(Verifier verifier);

	SimpleResult verify(Verifier verifier, CancelIndicator cancelIndicator, ProgressListener progressListener);

	
	/*
	 * Verify that each checksum in every payload manifest can be verified against
	 * the appropriate contents.
	 */
	SimpleResult checkPayloadManifests();

	SimpleResult checkPayloadManifests(CancelIndicator cancelIndicator, ProgressListener progressListener);

	
	/*
	 * Verify that each checksum in every tag manifest can be verified against
	 * the appropriate contents.
	 */	
	SimpleResult checkTagManifests();

	SimpleResult checkTagManifests(CancelIndicator cancelIndicator, ProgressListener progressListener);

	void load();
	
	void accept(BagVisitor visitor);
	
	void accept(BagVisitor visitor, CancelIndicator cancelIndicator, ProgressListener progressListener);
	
	Bag write(File file, Format format);
	
	Bag write(File file, Format format, CancelIndicator cancelIndicator, ProgressListener progressListener);

	Bag write(Writer writer, File file);
	
	Bag makeHoley(String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags);
	
	Bag makeHoley(String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags, ProgressListener progressListener, CancelIndicator cancelIndicator);
	
	Bag makeHoley(HolePuncher holePuncher, String baseUrl, boolean includePayloadDirectoryInUrl, boolean includeTags);
	
	Bag makeComplete();
	
	Bag makeComplete(ProgressListener progressListener, CancelIndicator cancelIndicator);
	
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
		Completer createCompleter();
		HolePuncher createHolePuncher();
		Writer createWriter(Format format);
		CompleteVerifier createCompleteVerifier();
		ValidVerifier createValidVerifier();
		ValidVerifier createValidVerifier(CompleteVerifier completeVerifier, ManifestChecksumVerifier manifestChecksumVerifier);
		ManifestChecksumVerifier createManifestVerifier();
		Version getVersion();				
	}
}