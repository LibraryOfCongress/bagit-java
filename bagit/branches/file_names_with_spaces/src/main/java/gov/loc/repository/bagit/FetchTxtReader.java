package gov.loc.repository.bagit;

import java.util.Iterator;

public interface FetchTxtReader extends Iterator<FetchTxt.FilenameSizeUrl> {

	void close();
	
}
