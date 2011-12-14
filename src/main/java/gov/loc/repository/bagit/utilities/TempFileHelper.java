package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;

public class TempFileHelper {
	public static File getTempFile(File file) {
		return new File(file.getPath() + ".biltemp");
	}

	public static void switchTemp(File file) {
		File tempFile = getTempFile(file);
		if (! tempFile.exists()) {
			throw new RuntimeException(MessageFormat.format("Temp file {0} for {1} does not exist.", tempFile, file));
		}
		try {
			if (file.exists()) {
				FileUtils.forceDelete(file);
			}
			FileUtils.moveFile(tempFile, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
