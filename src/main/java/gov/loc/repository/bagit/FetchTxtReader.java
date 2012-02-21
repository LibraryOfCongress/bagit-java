package gov.loc.repository.bagit;

import java.io.Closeable;
import java.util.Iterator;

public interface FetchTxtReader extends Iterator<FetchTxt.FilenameSizeUrl>, Closeable {

}
