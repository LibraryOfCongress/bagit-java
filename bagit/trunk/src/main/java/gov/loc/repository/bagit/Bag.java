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
	
	SimpleResult isValid(boolean missingBagItTolerant);
	
	SimpleResult isValid();
	
	SimpleResult isComplete(boolean missingBagItTolerant);
	
	SimpleResult isComplete();
	
	SimpleResult additionalVerify(List<VerifyStrategy> strategies);
	
	SimpleResult additionalVerify(VerifyStrategy strategy);
	
	void addPayload(File file);
	
	void addPayload(List<File> files);
	
	void complete();
	
	void complete(CompletionStrategy strategy);
	
	void write(BagWriter writer);
	
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