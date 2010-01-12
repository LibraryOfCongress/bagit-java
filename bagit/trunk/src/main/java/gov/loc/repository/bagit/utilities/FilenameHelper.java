package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilenameHelper {
	
	private static final Log log = LogFactory.getLog(FilenameHelper.class);
			
	public static String normalizePathSeparators(String filename) {
		if (filename == null) {
			return null;
		}
		String newFilename = FilenameUtils.separatorsToUnix(filename);			
		log.debug(MessageFormat.format("Normalized {0} to {1}", filename, newFilename));
		return newFilename;
	}
	
	public static String removeBasePath(String basePath, String filename) {
		if (filename == null) {
			throw new RuntimeException("Cannot remove basePath from null");
		}		
		String normBasePath = normalizePathSeparators(basePath);
		String normFilename = normalizePathSeparators(filename);
		String filenameWithoutBasePath = null;
		if (basePath == null) {
			filenameWithoutBasePath = normFilename;
		}
		else {
			if (! normFilename.startsWith(normBasePath)) {
				throw new RuntimeException(MessageFormat.format("Cannot remove basePath {0} from {1}", basePath, filename));
			}
			if (normBasePath.equals(normFilename)) {
				filenameWithoutBasePath = "";
			}
			else if (normBasePath.endsWith("/")){
				filenameWithoutBasePath = normFilename.substring(normBasePath.length());				
			} else {
				filenameWithoutBasePath = normFilename.substring(normBasePath.length() + 1);
			}
		}
		log.debug(MessageFormat.format("Removing {0} (normalized to {1}) from {2} (normalized to {3}) resulted in {4}", basePath, normBasePath, filename, normFilename, filenameWithoutBasePath));
		return filenameWithoutBasePath;
	}

	public static String normalizePath(String filepath) {
		int offset = 9;
		if (filepath.startsWith("/")) offset = 8;
		String newFilepath;
		try {
			newFilepath = new File("REMOVEME", filepath).getCanonicalPath();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return newFilepath.substring(newFilepath.indexOf("REMOVEME") + offset);
	}

}
