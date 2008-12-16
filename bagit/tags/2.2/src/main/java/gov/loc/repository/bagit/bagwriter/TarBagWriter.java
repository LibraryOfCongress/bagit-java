package gov.loc.repository.bagit.bagwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagWriter;

public class TarBagWriter implements BagWriter {

	private static final Log log = LogFactory.getLog(TarBagWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private TarOutputStream out = null;
	private String bagDir = null;
	
	public TarBagWriter(File bagFile) {
		this.bagDir = bagFile.getName().replaceFirst("\\..*$", "");
		try {
			File parentDir = bagFile.getParentFile();
			if (parentDir != null && ! parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			this.out = new TarOutputStream(new FileOutputStream(bagFile));
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public void close() {
		try {
			if (this.out != null) {
				this.out.close();
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
			//Add tar entry
			TarEntry entry = new TarEntry(this.bagDir + "/" + filepath);
			entry.setSize(bagFile.getSize());
			out.putNextEntry(entry);
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				this.out.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			out.closeEntry();
			in.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
