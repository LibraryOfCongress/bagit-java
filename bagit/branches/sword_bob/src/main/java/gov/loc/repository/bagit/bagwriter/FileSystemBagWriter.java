package gov.loc.repository.bagit.bagwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagWriter;

public class FileSystemBagWriter implements BagWriter {

	private static final Log log = LogFactory.getLog(FileSystemBagWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private File bagDir;
	private boolean skipIfPayloadFileExists = true;
	
	public FileSystemBagWriter(File bagDir, boolean skipIfPayloadFileExists) {
		this.skipIfPayloadFileExists = skipIfPayloadFileExists;
		this.bagDir = bagDir;
		try {
			if (bagDir.exists()) {
				if (! bagDir.isDirectory()) {
					throw new RuntimeException(MessageFormat.format("Bag directory {0} is not a directory.", bagDir.toString()));
				}
			} else {
				FileUtils.forceMkdir(bagDir);
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public void close() {
		//Do nothing

	}

	public void writePayloadFile(String filepath, BagFile bagFile) {
		File file = new File(this.bagDir, filepath);
		if (! this.skipIfPayloadFileExists || ! file.exists()) {
			log.debug(MessageFormat.format("Writing payload file {0} to {1}.", filepath, file.toString()));
			this.write(bagFile, file);	
		} else {
			log.debug(MessageFormat.format("Skipping writing payload file {0} to {1}.", filepath, file.toString()));
		}
		

		
	}
	
	public void writeTagFile(String filepath, BagFile bagFile) {
		File file = new File(this.bagDir, filepath);
		log.debug(MessageFormat.format("Writing tag file {0} to {1}.", filepath, file.toString()));		
		this.write(bagFile, file);				
	}
	
	private void write(BagFile bagFile, File file) {
		try {
			File parentDir = file.getParentFile();
			if (! parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			
			FileOutputStream out = new FileOutputStream(file);
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				out.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			in.close();
			out.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
