package gov.loc.repository.bagit.utilities;

import java.io.File;
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
	
	public static String normalizePath(String path, char seperator)
	{
		return doNormalize(path, seperator, false);
	}
	
	// Ganked from Commons IO FilenameUtil rev 723186
	// http://svn.apache.org/viewvc?view=revision&revision=723186
	// The new version will be in Commons IO 1.5, but it hasn't been released yet.
    private static final char SYSTEM_SEPARATOR = File.separatorChar;
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char OTHER_SEPARATOR;
    
    static {
        if (isSystemWindows()) {
            OTHER_SEPARATOR = UNIX_SEPARATOR;
        } else {
            OTHER_SEPARATOR = WINDOWS_SEPARATOR;
        }
    }

    static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }

    private static String doNormalize(String filename, char separator, boolean keepSeparator) {
        if (filename == null) {
            return null;
        }
        int size = filename.length();
        if (size == 0) {
            return filename;
        }
        int prefix = FilenameUtils.getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        
        char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
        filename.getChars(0, filename.length(), array, 0);
        
        // fix separators throughout
        char otherSeparator = (separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR);
        for (int i = 0; i < array.length; i++) {
            if (array[i] == otherSeparator) {
                array[i] = separator;
            }
        }
        
        // add extra separator on the end to simplify code below
        boolean lastIsDirectory = true;
        if (array[size - 1] != separator) {
            array[size++] = separator;
            lastIsDirectory = false;
        }
        
        // adjoining slashes
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == separator && array[i - 1] == separator) {
                System.arraycopy(array, i, array, i - 1, size - i);
                size--;
                i--;
            }
        }
        
        // dot slash
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == separator && array[i - 1] == '.' &&
                    (i == prefix + 1 || array[i - 2] == separator)) {
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                System.arraycopy(array, i + 1, array, i - 1, size - i);
                size -=2;
                i--;
            }
        }
        
        // double dot slash
        outer:
        for (int i = prefix + 2; i < size; i++) {
            if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' &&
                    (i == prefix + 2 || array[i - 3] == separator)) {
                if (i == prefix + 2) {
                    return null;
                }
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                int j;
                for (j = i - 4 ; j >= prefix; j--) {
                    if (array[j] == separator) {
                        // remove b/../ from a/b/../c
                        System.arraycopy(array, i + 1, array, j + 1, size - i);
                        size -= (i - j);
                        i = j + 1;
                        continue outer;
                    }
                }
                // remove a/../ from a/../c
                System.arraycopy(array, i + 1, array, prefix, size - i);
                size -= (i + 1 - prefix);
                i = prefix + 1;
            }
        }
        
        if (size <= 0) {  // should never be less than 0
            return "";
        }
        if (size <= prefix) {  // should never be less than prefix
            return new String(array, 0, size);
        }
        if (lastIsDirectory && keepSeparator) {
            return new String(array, 0, size);  // keep trailing separator
        }
        return new String(array, 0, size - 1);  // lose trailing separator
    }

}
