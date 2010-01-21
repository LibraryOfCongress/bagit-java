package gov.loc.repository.bagit.utilities;

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

	/**
	 * Normalizes a file path by replacing various special
	 * path tokens (., .., etc.) with their canonical equivalents.
	 * @param filepath The file path to normalize.
	 * @return The normalized file path.
	 */
	public static String normalizePath(String filepath) {
		if (filepath.startsWith("./") || filepath.startsWith(".\\")) {
			filepath = filepath.substring(2);
		}
		filepath = filepath.replace("/./", "/");
		filepath = filepath.replace("\\.\\", "\\");
		int endPos = filepath.indexOf("/../");
		while(endPos != -1) {
			int startPos = endPos-1;
			while(startPos >= 0 && '/' != filepath.charAt(startPos)) {
				startPos--;
			}
			if (startPos > 0) {
				filepath = filepath.substring(0,startPos) + "/" + filepath.substring(endPos+4);
			} else {
				filepath = filepath.substring(endPos+4);
			}
			endPos = filepath.indexOf("/../");
		}
		endPos = filepath.indexOf("\\..\\");
		while(endPos != -1) {
			int startPos = endPos-1;
			while(startPos >= 0 && '\\' != filepath.charAt(startPos)) {
				startPos--;
			}
			if (startPos > 0) {
				filepath = filepath.substring(0,startPos) + "\\" + filepath.substring(endPos+4);
			} else {
				filepath = filepath.substring(endPos+4);
			}
			endPos = filepath.indexOf("\\..\\");
		}
		return filepath;
	}

}
