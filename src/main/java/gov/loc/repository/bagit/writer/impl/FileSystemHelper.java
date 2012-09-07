package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.utilities.TempFileHelper;

public class FileSystemHelper {

	private static final Log log = LogFactory.getLog(FileSystemHelper.class);
	
	private static final int BUFFERSIZE = 65536;
		
	public static void write(BagFile bagFile, File file) {
		File parentDir = file.getParentFile();
		if (! parentDir.exists()) {
			try {
				FileUtils.forceMkdir(parentDir);
			} catch (IOException ex) {
				String msg = MessageFormat.format("Error creating {0}: {1}", parentDir, ex.getMessage());
				log.error(msg);
				throw new RuntimeException(msg, ex);
			}
		}
		
		File tempFile = TempFileHelper.getTempFile(file);
		FileOutputStream out;
		try {
			out = new FileOutputStream(tempFile);
		} catch (FileNotFoundException ex) {
			String msg = MessageFormat.format("Error opening {0} for writing: {1}", tempFile, ex.getMessage(), ex);
			log.error(msg);
			throw new RuntimeException(msg, ex);
		}
		InputStream in = bagFile.newInputStream();
		try {
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				out.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
		} catch (Exception ex) {
			String msg = MessageFormat.format("Error writing {0} to temp file {1}: {2}", bagFile.getFilepath(), tempFile, ex.getMessage()); 
			log.error(msg);
			throw new RuntimeException(msg, ex);				
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		TempFileHelper.switchTemp(file);
		
	}

	public static void copy(File sourceFile, File file) {
		if(sourceFile.equals(file)) {
			throw new RuntimeException(MessageFormat.format("Cannot copy {0} to itself", sourceFile));
		}
		try {
			FileUtils.copyFile(sourceFile, file, true);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("Error copying {0} to {1}: {2}", sourceFile, file, e.getMessage()), e);
		}
	}
	
	public static void move(File sourceFile, File file) {
		if(sourceFile.equals(file)) {
			throw new RuntimeException(MessageFormat.format("Cannot move {0} to itself", sourceFile));
		}
		try {
			FileUtils.moveFile(sourceFile, file);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("Error copying {0} to {1}: {2}", sourceFile, file, e.getMessage()), e);
		}
	}

}
