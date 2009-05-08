package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.text.MessageFormat;

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
	
	public static FileObject getFileObjectForBag(File fileForBag) {
		if (fileForBag == null) {
			throw new RuntimeException("No file was provided for this bag");
		}
		
		if (! fileForBag.exists()) {
			throw new RuntimeException(MessageFormat.format("{0} does not exist", fileForBag));
		}
		
		FileObject fileObject = getFileObject(fileForBag, true);		
		try {
			
			//If a serialized bag, then need to get bag directory from within
			Format format = FormatHelper.getFormat(fileForBag);
			if (format.isSerialized) {
				if (fileObject.getChildren().length != 1) {
					throw new RuntimeException("Unable to find bag_dir in serialized bag");
				}
				return fileObject.getChildren()[0];
			}
			return fileObject;													
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}

	}

}
