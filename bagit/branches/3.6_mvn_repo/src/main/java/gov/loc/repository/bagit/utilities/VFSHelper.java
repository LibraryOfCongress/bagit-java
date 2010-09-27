package gov.loc.repository.bagit.utilities;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;

import gov.loc.repository.bagit.Bag.Format;

public class VFSHelper {
	
	private static final Log log = LogFactory.getLog(VFSHelper.class);
	
	/**
	 * Thread local variable to store a {@link FileSystemManager}.  This solves the problem
	 * of VFS not being thread-safe.
	 */
	private static final ThreadLocal<FileSystemManager> fileSystemManager = new ThreadLocal<FileSystemManager>() {
		@Override
		protected FileSystemManager initialValue() {
			StandardFileSystemManager mgr = new StandardFileSystemManager();
			mgr.setLogger(LogFactory.getLog(VFS.class));
			
			try
			{
				mgr.init();
			}
			catch (FileSystemException e)
			{
				log.fatal("Could not initialize thread-local FileSystemManager.", e);
			}
			
			return mgr;
		}
	};

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
			FileSystemManager fsManager = fileSystemManager.get();
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
	
	public static void closeFileSystemManager() {
		((DefaultFileSystemManager)(fileSystemManager.get())).close();
	}
	
	public static FileObject getFileObject(File file, boolean flushCache) {		
		//Get the bag URI
		String fileURI = getUri(file);
		
		try {
			//Open the fileObject
			FileSystemManager fsManager = fileSystemManager.get();
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
