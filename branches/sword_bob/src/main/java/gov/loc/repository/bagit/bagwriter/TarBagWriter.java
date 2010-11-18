package gov.loc.repository.bagit.bagwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagWriter;

public class TarBagWriter implements BagWriter {

	private static final Log log = LogFactory.getLog(TarBagWriter.class);
	
	private static final int BUFFERSIZE = 65536;
	
	private OutputStream out = null;
	private TarOutputStream tarOut = null;
	private String bagDir = null;
	
	public TarBagWriter(File bagFile) {
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
	
	public TarBagWriter(String bagDir, OutputStream out) {
		this.bagDir = bagDir;
		this.out = out;
	}
	
	public void open(Bag bag) {
		this.tarOut = new TarOutputStream(this.out);
	}
	
	public void close() {
		try {
			if (this.tarOut != null) {
				this.tarOut.close();
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
			tarOut.putNextEntry(entry);
			InputStream in = bagFile.newInputStream();
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				this.tarOut.write(dataBytes, 0, nread);
			    nread = in.read(dataBytes);
			}
			tarOut.closeEntry();
			in.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
