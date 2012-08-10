package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHelper {

	private static final Log log = LogFactory.getLog(FileHelper.class);
	
	public static File normalizeForm(File file) {
		if (file.exists()) {
			log.debug(MessageFormat.format("No problem with form of filename for {0}", file));
			return file;
		}
		File nfcFile = new File(Normalizer.normalize(file.getAbsolutePath(), Form.NFC));
		if (nfcFile.exists()) {
			log.debug(MessageFormat.format("Using NFC form of filename for {0}", file));
			return nfcFile;
		}
		File nfdFile = new File(Normalizer.normalize(file.getAbsolutePath(), Form.NFD));
		if (nfdFile.exists()) {
			log.debug(MessageFormat.format("Using NFD form of filename for {0}", file));
			return nfdFile;
		}
		return file;
	}
}
