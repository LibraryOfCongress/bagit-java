package gov.loc.repository.bagit.filesystem;

import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.filesystem.impl.FileFileSystem;
import gov.loc.repository.bagit.filesystem.impl.ZipFileSystem;
import gov.loc.repository.bagit.utilities.FormatHelper;
import gov.loc.repository.bagit.utilities.FormatHelper.UnknownFormatException;

import java.io.File;
import java.text.MessageFormat;

public class FileSystemFactory {

	public static DirNode getDirNodeForBag(File fileForBag) throws UnknownFormatException, UnsupportedFormatException {
		assert fileForBag != null;
		
		if (! fileForBag.exists()) {
			throw new RuntimeException(MessageFormat.format("{0} does not exist", fileForBag));
		}

		Format format = FormatHelper.getFormat(fileForBag);
		FileSystem fs = null;
		if (Format.FILESYSTEM == format) {
			fs = new FileFileSystem(fileForBag);
		} else if (Format.ZIP == format) {
			fs = new ZipFileSystem(fileForBag);
		} else {
			throw new UnsupportedFormatException();
		}

		DirNode root = fs.getRoot();
		if (format.isSerialized) {
			if (root.listChildren().size() != 1) {
				throw new RuntimeException("Unable to find bag_dir in serialized bag");
			}
			FileSystemNode bagDirNode = root.listChildren().iterator().next();
			if (! (bagDirNode instanceof DirNode)) {
				throw new RuntimeException("Unable to find bag_dir in serialized bag");
			}
			root = (DirNode)bagDirNode;
		}
		return root;
	}

	public static class UnsupportedFormatException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnsupportedFormatException() {
			super("Unsupported format");
		}
	}

	
}
