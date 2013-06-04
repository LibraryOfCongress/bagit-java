package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHelper {

	private static final Log log = LogFactory.getLog(FileHelper.class);
	
	public static File normalizeForm(File file) {
		if (file == null) return file;
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
	
	public static File[] normalizeForm(File[] files) {
		if (files == null) return null;
		for(int i=0; i < files.length; i++) {
			files[i] = normalizeForm(files[i]);
		}
		return files;
	}
	
	public static Collection<File> normalizeForm(Collection<File> files) {
		if (files == null) return null;
		Collection<File> newFiles = new ArrayList<File>(files.size());
		for(File file : files) {
			newFiles.add(normalizeForm(file));
		}		
		return newFiles;
	}

}
