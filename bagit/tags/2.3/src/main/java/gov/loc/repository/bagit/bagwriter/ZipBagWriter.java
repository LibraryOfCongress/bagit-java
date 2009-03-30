package gov.loc.repository.bagit.bagwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagWriter;

public class ZipBagWriter implements BagWriter {

	private static final Log log = LogFactory.getLog(ZipBagWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private OutputStream out = null;
	private ZipOutputStream zipOut = null;
	private String bagDir = null;
	
	public ZipBagWriter(File bagFile) {
		this.bagDir = bagFile.getName().replaceFirst("\\..*$", "");
		try {
			File parentDir = bagFile.getParentFile();
			if (parentDir != null && ! parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			this.out = new FileOutputStream(bagFile);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public ZipBagWriter(String bagDir, OutputStream out) {
		this.bagDir = bagDir;
		this.out = out;		
	}
	
	public void open(Bag bag) {
		this.zipOut = new ZipOutputStream(this.out);
	}
	
	public void close() {
		try {
			if (this.zipOut != null) {
				this.zipOut.close();
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void writePayloadFile(String filepath, BagFile bagFile) {
		log.debug(MessageFormat.format("Writing payload file {0}.", filepath));
		this.write(filepath, bagFile);		
	}
	
	public void writeTagFile(String filepath, BagFile bagFile) {
		log.debug(MessageFormat.format("Writing tag file {0}.", filepath));
		this.write(filepath, bagFile);				
	}
	
	private void write(String filepath, BagFile bagFile) {
		try {
			//Add zip entry
			zipOut.putNextEntry(new ZipEntry(this.bagDir + "/" + filepath));
			
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				this.zipOut.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			in.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
