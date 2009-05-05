package gov.loc.repository.bagit.utilities;

import java.io.File;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import gov.loc.repository.bagit.Bag.Format;

public class VFSHelper {
	
	public static String getUri(File file) {
		return getUri(file, FormatHelper.getFormat(file));
	}

	public static String getUri(File file, Format format) {
		return file.toURI().toString().replaceFirst(Format.FILESYSTEM.scheme, format.scheme);
	}
	
	public static String concatUri(String baseURI, String filepath) {		
		String delim = "!";
		if (baseURI.startsWith(Format.FILESYSTEM.scheme)) {
			delim = "/";
		}
		return baseURI + delim + filepath; 
	}
	
	public static FileObject getFileObject(String fileURI, boolean flushCache) {		
		try {
			//Open the fileObject
			FileSystemManager fsManager = VFS.getManager();
			if (flushCache) {
				fsManager.getFilesCache().clear(fsManager.resolveFile(fileURI).getFileSystem());
			}
			return fsManager.resolveFile(fileURI);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static FileObject getFileObject(String fileURI) {		
		return getFileObject(fileURI, false);
	}

	public static FileObject getFileObject(File file) {		
		return getFileObject(file, true);
	}
	
	public static FileObject getFileObject(File file, boolean flushCache) {		
		//Get the bag URI
		String fileURI = getUri(file);
		
		try {
			//Open the fileObject
			FileSystemManager fsManager = VFS.getManager();
			if (flushCache) {
				fsManager.getFilesCache().clear(fsManager.resolveFile(fileURI).getFileSystem());
			}
			return fsManager.resolveFile(fileURI);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
