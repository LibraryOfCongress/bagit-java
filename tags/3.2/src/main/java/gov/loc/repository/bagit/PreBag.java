package gov.loc.repository.bagit;

import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.transformer.Completer;

import java.io.File;
import java.util.List;

public interface PreBag {
	void setFile(File file);
	File getFile();
	void setTagFiles(List<File> tagFiles);
	List<File> getTagFiles();
	Bag makeBagInPlace(Version version, boolean retainBaseDirectory);
	Bag makeBagInPlace(Version version, boolean retainBaseDirectory, Completer completer);
}
