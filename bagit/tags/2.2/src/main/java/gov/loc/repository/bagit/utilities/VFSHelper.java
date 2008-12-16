package gov.loc.repository.bagit.utilities;

import java.io.File;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import gov.loc.repository.bagit.Bag.Format;

public class VFSHelper {
	public static String getUri(File file, Format format) {
		return file.toURI().toString().replaceFirst(Format.FILESYSTEM.scheme, format.scheme);
	}
	
	public static FileObject getFileObject(File file) {
		Format format = FormatHelper.getFormat(file);
		
		//Get the bag URI
		String bagURI = VFSHelper.getUri(file, format);
		
		try {
			//Open the fileObject
			FileSystemManager fsManager = VFS.getManager();
			fsManager.getFilesCache().clear(fsManager.resolveFile(bagURI).getFileSystem());
			return fsManager.resolveFile(bagURI);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}

	}

}
